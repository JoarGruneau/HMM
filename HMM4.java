import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;


public class HMM4 {

    public static void main(String[] args) {
        String file_location_felix = 
                "D:/Felix/Programming/Workspace/HMM4/data/sample00_4";
        String file_location_joar="/home/joar/Documents/AI/test.txt";
        boolean use_std_input = true;
        readAndSolve(use_std_input, file_location_felix);
    }
    public static void readAndSolve(boolean use_std_input, String file_location){
        Scanner data = null;
        if(use_std_input){
            data = new Scanner(System.in);
        }else{
            try{
                data = new Scanner(new File(file_location));
            }catch ( IOException e){
                System.out.println("Could not open file.");
                e.printStackTrace();
                System.exit(0);
            }
        }
        Matrix a_est=new Matrix(readInputMatrix(data.nextLine()));
        Matrix b_est=new Matrix(readInputMatrix(data.nextLine()));
        Matrix pi_est=new Matrix(readInputMatrix(data.nextLine()));
        int[] obs_seq = readObservationSeq(data.nextLine());
        data.close();
        AlphaPass alpha;
        BetaPass beta;
        GammaCalc gammas;

        int safe_counter = 0;
        double old_log_prob = -100000000000.0; // Initialize as highly improbable
        double epsilon = 0.00001;
        while(safe_counter < 10000){
            // Calculate the alphas, betas, gammas based on current estimates of lambda
            alpha =new AlphaPass(a_est,b_est,pi_est,obs_seq);
            beta =new BetaPass(a_est,b_est,pi_est,obs_seq,alpha.scale_factors);
            gammas = new GammaCalc(a_est,b_est,pi_est,obs_seq,alpha,beta);
            a_est = estimateA(gammas);
            b_est = estimateB(gammas, obs_seq, b_est);
            pi_est = estimatePi(gammas);

            // Later add a constraint on abs(log_prob-old_log_prob)<epsilon which breaks loop
            double new_log_prob = alpha.log_probability;
            if(Math.abs(old_log_prob-new_log_prob) < epsilon){
                //System.out.println("Converged.");
                break;
            }else{
                old_log_prob = new_log_prob;                
            }
            safe_counter += 1;
        }
        a_est = a_est.roundMatrixElements(6);
        a_est.printAsString();
        b_est = b_est.roundMatrixElements(6);
        b_est.printAsString();
        //System.out.println(alpha.log_probability);
    }


    public static double[][] readInputMatrix(String in_string_matrix){
        String[] listMatrix=in_string_matrix.split(" ");
        int rows=Integer.valueOf(listMatrix[0]);
        int columns=Integer.valueOf(listMatrix[1]);
        int counter=2;
        double[][] out_matrix = new double[rows][columns];
        for(int j=0;j<rows;j++){
            for(int k=0;k<columns;k++){
                out_matrix[j][k]=Double.valueOf(listMatrix[counter]);
                counter++;
            }
        }
        return out_matrix;
    }
    public static int[] readObservationSeq(String in_string_matrix){
        String[] listMatrix=in_string_matrix.split(" ");
        int rows=Integer.valueOf(listMatrix[0]);
        int counter=1;
        int[] out_array = new int[rows];
        for(int j=0;j<rows;j++){
            out_array[j]=Integer.valueOf(listMatrix[counter]);
            counter++;
        }
        return out_array;
    }

    public static Matrix estimateA(GammaCalc gammas){
        int nr_states = gammas.di_gamma[0].matrix.length;
        double[][] a_matrix_tmp = new double[nr_states][nr_states];
        double current_denom;
        double tmp_a_ij;
        for(int i=0; i<nr_states; i++){
            for(int j=0; j<nr_states; j++){
                current_denom = 0.0;
                tmp_a_ij = 0.0;
                for(int t=0; t<gammas.di_gamma.length; t++){
                    tmp_a_ij += gammas.di_gamma[t].matrix[i][j];
                    current_denom += gammas.gamma[t].matrix[0][i];
                }
                a_matrix_tmp[i][j] = tmp_a_ij / current_denom;
            }
        }
        return new Matrix(a_matrix_tmp);
    }
    public static Matrix estimateB(GammaCalc gammas, int[] obs_seq, Matrix b_est){
        int nr_states = gammas.di_gamma[0].matrix.length;
        int nr_emissions = b_est.columns;
        double[][] b_matrix_tmp = new double[nr_states][nr_emissions];
        double current_denom;
        double tmp_b_ij;
        for(int i=0; i<nr_states; i++){
            for(int j=0; j<nr_emissions; j++){ 
                current_denom = 0.0;
                tmp_b_ij = 0.0;
                for(int t=0; t<gammas.gamma.length; t++){
                    if(obs_seq[t] == j){
                        tmp_b_ij += gammas.gamma[t].matrix[0][i];
                    }
                    current_denom += gammas.gamma[t].matrix[0][i];
                }
                b_matrix_tmp[i][j] = tmp_b_ij / current_denom;
            }
        }
        return new Matrix(b_matrix_tmp);
    }
    public static Matrix estimatePi(GammaCalc gammas){
        return gammas.gamma[0];
    }
    
}
class AlphaPass{
    public final double log_probability;
    public final Matrix[] alpha_container;
    public final double[] scale_factors;
    public AlphaPass(Matrix a, Matrix b, Matrix pi, int[] obs_seq){
        Matrix[] tmp_alpha_container = new Matrix[obs_seq.length];
        double[] alpha_scale=new double[obs_seq.length];
        tmp_alpha_container[0] = pi.multiplyColumn(b, obs_seq[0]); /*initialize alpha 0*/
        alpha_scale[0]=1/tmp_alpha_container[0].sumRow(0);
        tmp_alpha_container[0].scale(alpha_scale[0]);
        for (int i = 1; i<obs_seq.length; i++){
            tmp_alpha_container[i] = tmp_alpha_container[i-1].multiply(a).
                    multiplyColumn(b, obs_seq[i]);
            alpha_scale[i]=1/tmp_alpha_container[i].sumRow(0);
            tmp_alpha_container[i].scale(alpha_scale[i]);
            
        }
        double tmp_log=0;
        for(double item:alpha_scale){
            tmp_log=tmp_log -Math.log(item);
                
        }
        log_probability=tmp_log;
        alpha_container=tmp_alpha_container;
        scale_factors=alpha_scale;
    }
}
class BetaPass{
    public final Matrix[] beta_container;
    public final double[] scale_factors;
    
    public BetaPass(Matrix a, Matrix b, Matrix pi, int[] obs_seq, double[] alpha_scale){        
        int obs_length=obs_seq.length;
        double[][] initial= new double[1][pi.columns];
        scale_factors=alpha_scale;
        Arrays.fill(initial[0], 1); /*Filling beta_(T-1) with 1s*/
        Matrix[] tmp_beta_container=new Matrix[obs_seq.length];
        //initialize beta_(T-1)
        tmp_beta_container[obs_length-1]=new Matrix(initial);
        tmp_beta_container[obs_length-1].scale(scale_factors[obs_length-1]);
        for(int i=obs_length-2;i>-1;i--){
            tmp_beta_container[i]=tmp_beta_container[i+1].
                    transposeMultiply(a.multiplyColumn(b,obs_seq[i+1]));
            tmp_beta_container[i].scale(scale_factors[i]);
        }
        beta_container=tmp_beta_container;
        
        // To be defined. Should return Matrix[]
    }
}
class GammaCalc{
    Matrix[] di_gamma; // obs_seq*N*N matrix
    Matrix[] gamma; // obs_seq*1*N matrix

    public GammaCalc(Matrix a, Matrix b, Matrix pi, int[] obs_seq, AlphaPass alpha, 
                        BetaPass beta){
        double[][] tmp_di_gamma_matrix;
        double[][] tmp_gamma_matrix;
        double gamma_scale;
        int obs_length=obs_seq.length;
        di_gamma = new Matrix[obs_length-1];
        gamma = new Matrix[obs_length];
        for(int t=0; t<obs_length-1; t++){
            // Calculating gamma_scale for current t
            gamma_scale = 0.0;
            for (int i = 0; i < a.rows; i++){ /*i is current state*/
                for(int j = 0; j<a.columns; j++){ /*j is the next state*/
                    gamma_scale += alpha.alpha_container[t].matrix[0][i]*a.matrix[i][j]*
                        b.matrix[j][obs_seq[t+1]]*beta.beta_container[t+1].matrix[0][j];
                }
            }
            // Calculating the gamma matrices
            tmp_di_gamma_matrix = new double[a.rows][a.columns];
            tmp_gamma_matrix = new double[1][a.columns];
            for (int i=0; i<a.rows; i++){
                for(int j=0; j<a.columns; j++){
                    //System.out.println(tmp_gamma_matrix[0][i]);
                    //System.out.println(j);
                    tmp_di_gamma_matrix[i][j] = alpha.alpha_container[t].matrix[0][i]*a.matrix[i][j]*
                        b.matrix[j][obs_seq[t+1]]*beta.beta_container[t+1].matrix[0][j]/gamma_scale;
                    tmp_gamma_matrix[0][i] += tmp_di_gamma_matrix[i][j];
                }
            }
            di_gamma[t] = new Matrix(tmp_di_gamma_matrix);
            gamma[t] = new Matrix(tmp_gamma_matrix);
        }
        // Computing gamma_(T-1), the last one
        double denom = 0.0;
        for (int i=0; i<a.rows; i++){
            denom += alpha.alpha_container[obs_length-1].matrix[0][i];
        }
        tmp_gamma_matrix = new double[1][a.columns];
        for (int i=0; i<a.rows; i++){
            tmp_gamma_matrix[0][i] = alpha.alpha_container[obs_length-1].matrix[0][i]/denom;
        }
        gamma[obs_length-1] = new Matrix(tmp_gamma_matrix);
        return;
    }
}

class Matrix {
    public final double[][] matrix;
    public final int rows;
    public final int columns;

    public Matrix(double[][] in_matrix){
        matrix = in_matrix;
        rows = matrix.length;
        columns = matrix[0].length;
    }

    public Matrix multiply(Matrix inMatrix){//returns Matrix*inMatrix
      double[][] out_matrix = new double[rows][inMatrix.columns];
        for(int h=0;h<inMatrix.columns;h++){
            for(int i=0; i<rows;i++){
            double tmp_sum=0;
                for(int j=0;j<columns;j++){
                    tmp_sum=tmp_sum+inMatrix.matrix[j][h]*matrix[i][j];
                }
            out_matrix[i][h] = tmp_sum; /*same row index as Matrix, same column index as inMatrix*/
            }
        }
      return new Matrix(out_matrix);
    }
    public Matrix multiplyElementwise(Matrix inMatrix){
        /*Returns matrix.*inMatrix as a row vector. Both matrix and inMatrix need to be row vectors of same dimension*/
        double[][] out_matrix = new double[rows][columns];
        for(int j = 0; j<columns; j++){
            out_matrix[0][j] = matrix[0][j]*inMatrix.matrix[0][j];
        }
        return new Matrix(out_matrix);

    }

    public void printAsString(){
        String print_str = "";
        print_str += rows + " " + columns;
        for(int i=0; i<rows; i++){
            for(int j=0; j<columns; j++){
                print_str += " " + matrix[i][j];
            }
        }
        System.out.println(print_str);
    }

    public void print(){
        for(double[] row: matrix){
            System.out.println(Arrays.toString(row));
        }
    }
    public void printSum(){
        /*Prints the sum of the row elements in a row vector*/
        double sum = 0;
        for(double elem:matrix[0]){
            sum += elem;
        }
        System.out.println(sum);
    }
    public Matrix multiplyColumn(Matrix in_matrix, int in_column){
        double[][] out_matrix=new double[rows][columns];
        for(int h=0; h<rows; h++){ //loops through rows in it own matrix
            //loops through columns in its own matrix
            for(int i = 0; i<columns; i++){
                out_matrix[h][i] = this.matrix[h][i]*in_matrix.matrix[i][in_column];
            }
        }
        return new Matrix(out_matrix);
    }
    public Matrix getColumn(int column_idx){
        /*Returns the column of a given matrix at a given index as a row vector matrix*/
        double[][] out_matrix = new double[1][rows];
        for(int i = 0; i<rows; i++){
            out_matrix[0][i] = matrix[i][column_idx];
        }
        return new Matrix(out_matrix);
    }
    public double sumRow(int row){
        double sum = 0;
        for(double elem:matrix[row]){
            sum += elem;
        }
        return sum;   
    } 
    public Matrix transposeMultiply(Matrix in_matrix){
        //computes matrix multiplication of matrix*inmatrix_transponat.
        double[][] out_matrix = new double[rows][in_matrix.rows];
        for(int i=0; i<rows; i++){
            for(int j = 0; j<in_matrix.rows; j++){
                double tmp_sum=0;
                for(int k=0; k<columns;k++){
                    tmp_sum=tmp_sum+matrix[i][k]*in_matrix.matrix[j][k];
                }
            out_matrix[i][j] =tmp_sum;
            }
        }
        return new Matrix(out_matrix);
    }
    public void scale(double scale){
        for(int i=0; i<rows; i++){
            for(int j=0;j<columns;j++){
                matrix[i][j]=matrix[i][j]*scale;
            }
        }
    }
    public Matrix roundMatrixElements(int nr_digits){
        String decimal_format_string = "#.";
        for(int i = 0; i<nr_digits; i++){
            decimal_format_string += "#";
        }
        DecimalFormat new_format = new DecimalFormat(decimal_format_string);
        double[][] tmp_matrix = new double[rows][columns];
        for(int i=0; i<rows; i++){
            for(int j=0;j<columns;j++){
                tmp_matrix[i][j] = Double.parseDouble(new_format.format(matrix[i][j]));
            }
        }
        return new Matrix(tmp_matrix);
    }
}