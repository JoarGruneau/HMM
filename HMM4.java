import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class HMM4 {


    public static void main(String[] args) {
    	String file_location_felix = 
                "D:/Felix/Programming/Workspace/HMM4/data/sample00_4";
        String file_location_joar="/home/joar/Documents/AI/test.txt";
    	boolean use_std_input = false;
        readAndSolve(use_std_input, file_location_joar);
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
        Matrix a=new Matrix(readInputMatrix(data.nextLine()));
        Matrix b=new Matrix(readInputMatrix(data.nextLine()));
        Matrix pi=new Matrix(readInputMatrix(data.nextLine()));
        int[] obs_seq = readObservationSeq(data.nextLine());
        data.close();

        AlphaPass alpha =new AlphaPass(a,b,pi,obs_seq);
        BetaPass beta =new BetaPass(a,b,pi,obs_seq);
        System.out.println(beta.log_probability);
        System.out.println(alpha.log_probability);

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
    
}
class BetaPass{
    public final double log_probability;
    public final Matrix[] beta_container;
    
    public BetaPass(Matrix a, Matrix b, Matrix pi, int[] obs_seq){        
        int obs_length=obs_seq.length;
        double[][] initial= new double[1][pi.columns];
        double[] beta_scale=new double[obs_length];
        Arrays.fill(initial[0], 1);
        Matrix[] tmp_beta_container=new Matrix[obs_seq.length];
        //initialize beta_(T-1)
        tmp_beta_container[obs_length-1]=new Matrix(initial);
        beta_scale[obs_length-1]=1/tmp_beta_container[obs_length-1].sumRow(0);
        tmp_beta_container[obs_length-1].scale(beta_scale[obs_length-1]);
        for(int i=obs_length-2;i>-1;i--){
            tmp_beta_container[i]=tmp_beta_container[i+1].
                    transponatMultiply(a.multiplyColumn(b,obs_seq[i+1]));
            beta_scale[i]=1/tmp_beta_container[i].sumRow(0);
            tmp_beta_container[i].scale(beta_scale[i]);
        }
        double tmp_log=0;
        for(double item:beta_scale){
            tmp_log=tmp_log -Math.log(item);
                
        }
        log_probability=tmp_log;
        beta_container=tmp_beta_container;
        
        
        // To be defined. Should return Matrix[]
    }
}
class AlphaPass{
    public final double log_probability;
    public final Matrix[] alpha_container;
    public AlphaPass(Matrix a, Matrix b, Matrix pi, int[] obs_seq){
        Matrix[] tmp_alpha_container = new Matrix[obs_seq.length];
        double[] alpha_scale=new double[obs_seq.length];
        tmp_alpha_container[0] = pi.multiplyColumn(b, obs_seq[0]); /*initialize alpha 1*/
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
        return;
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
    public Matrix transponatMultiply(Matrix in_matrix){
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
}