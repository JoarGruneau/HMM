import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class HMM4 {


    public static void main(String[] args) {
    	String file_location_felix = 
                "D:/Felix/Programming/Workspace/HMM4/data/sample00_4";
        String file_location_joar="/home/joar/Documents/AI/test.txt";
    	boolean use_std_input = true;
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

        Matrix[] alpha_container = alphaPass(a,b,pi,obs_seq);
        alpha_container[alpha_container.length-1].printSum();

    }


    public static Matrix[] alphaPass(Matrix a, Matrix b, Matrix pi, int[] obs_seq){
        Matrix[] alpha_container = new Matrix[obs_seq.length];
        double[] norm=new double[obs_seq.length];
        alpha_container[0] = pi.multiplyColumn(b, obs_seq[0]); /*initialize alpha 1*/
        norm[0]=alpha_container[0].sumRow(0);
        for (int i = 1; i<obs_seq.length; i++){
            alpha_container[i] = alpha_container[i-1].multiply(a).
                    multiplyColumn(b, obs_seq[i]);
            //norm[i]=alpha_container[i].sumRow(0);
            //System.out.println(norm[i]);
        }
        

        return alpha_container;
    }

    public static void betaPass(Matrix a, Matrix b, Matrix pi, int[] obs_seq){
        // To be defined. Should return Matrix[]
    	return;
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
        double[][] out_matrix=new double[1][columns];
        for(int i = 0; i<columns; i++){
            out_matrix[0][i] = this.matrix[0][i]*in_matrix.matrix[i][in_column];
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
}