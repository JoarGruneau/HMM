/*import java.util.Arrays;
import java.util.Scanner;*/
import java.util.*;
import java.io.File;
import java.io.IOException;

public class HMM3 {


    public static void main(String[] args) {
    	String file_location = "D:/Felix/Programming/Workspace/HMM3/data/sample00_3";
    	boolean use_std_input = true;
        readAndSolve(use_std_input, file_location);
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
        Matrix a=new Matrix(data.nextLine());
        Matrix b=new Matrix(data.nextLine());
        Matrix pi=new Matrix(data.nextLine());
        int[] obs_seq = readObservationSeq(data.nextLine());
        data.close();

        Matrix[] delta_container = new Matrix[obs_seq.length];
        int[][] delta_idx_container = new int[obs_seq.length-1][a.rows];
        delta_container[0] = pi.multiplyElementwise(b.getColumn(obs_seq[0])); //Initialize delta_1
 
        for (int i = 1; i<obs_seq.length; i++){
             DeltaVariables next_values = delta_container[i-1].calculateNextDelta(a, b.getColumn(obs_seq[i]));
             delta_container[i] = next_values.delta;
             delta_idx_container[i-1] = next_values.delta_idx;
        }
        //delta_container[obs_seq.length-1].print();
        printMostLikelySequence(delta_idx_container, delta_container);
 
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

    public static void printMostLikelySequence(int[][] delta_idx_container, Matrix[] delta_container){
        int[] most_likely_sequence = new int[delta_container.length];
        int max_idx = 0;
        float max_value = 0;
        for(int i = 0; i<delta_container[delta_container.length-1].matrix[0].length; i++){
            float element = delta_container[delta_container.length-1].matrix[0][i];
            if(element > max_value){
                max_value = element;
                max_idx = i;
            }
        }
        most_likely_sequence[most_likely_sequence.length-1] = max_idx;

        for(int i = delta_idx_container.length-1; i>-1; i--){
            most_likely_sequence[i] = delta_idx_container[i][most_likely_sequence[i+1]];
        }
        String print_str ="";
        for(int i:most_likely_sequence){
            print_str += i+" ";
        }
        System.out.println(print_str);
    }
    
}
class Matrix {
    public final float[][] matrix;
    public final int rows;
    public final int columns;
    public final String string_matrix;
    
    public Matrix(String in_string_matrix){
        String[] listMatrix=in_string_matrix.split(" ");
        rows=Integer.valueOf(listMatrix[0]);
        columns=Integer.valueOf(listMatrix[1]);
        int counter=2;
        string_matrix=in_string_matrix;
        matrix=new float[rows][columns];
        for(int j=0;j<rows;j++){
            for(int k=0;k<columns;k++){
                matrix[j][k]=Float.valueOf(listMatrix[counter]);
                counter++;
            }
        }
    }
    public Matrix multiply(Matrix inMatrix){//returns Matrix*inMatrix
      String build_string=rows+ " " + inMatrix.columns;
        for(int h=0;h<inMatrix.columns;h++){
            for(int i=0; i<rows;i++){
            float tmp_sum=0;
                for(int j=0;j<columns;j++){
                    tmp_sum=tmp_sum+inMatrix.matrix[j][h]*matrix[i][j];
                }
            build_string=build_string+" "+ tmp_sum;
            }  
        }
      return new Matrix(build_string);
    }
    public Matrix multiplyElementwise(Matrix inMatrix){
        /*Returns matrix.*inMatrix as a row vector. Both matrix and inMatrix need to be row vectors of same dimension*/
        String build_string=rows+ " " + columns;
        float element_product;
        for(int j = 0; j<columns; j++){
            element_product = matrix[0][j]*inMatrix.matrix[0][j];
            build_string=build_string+" "+ Float.toString(element_product);
        }
        return new Matrix(build_string);

    }

    public String string(){
        return string_matrix;
    }
    public void print(){
        for(float[] row: matrix){
            System.out.println(Arrays.toString(row));
        }
    }
    public Matrix getColumn(int column_idx){
        /*Returns the column of a given matrix at a given index as a row vector matrix*/
        String build_string=Integer.toString(1)+ " " + Integer.toString(rows);
        for(float[] row:matrix){
            build_string = build_string + " " +row[column_idx];
        }
        return new Matrix(build_string);
    }
    public DeltaVariables calculateNextDelta(Matrix a, Matrix b_col){
        /*a is NxN, b_col is 1xN. Returns a matrix with 1xN*/
    	float[] max_values =new float[columns]; // all values set to 0 by default. Each element is the max of the column vector in "a" multiplied by delta(j)
        int[] max_indices = new int[columns];
        float current_value;
        String build_string=Integer.toString(1)+ " " + Integer.toString(a.columns);
        for(int i = 0; i<a.rows; i++){ /*i is row number of a, column number of delta*/
            for(int j=0; j<a.columns; j++){
                current_value = a.matrix[i][j]*matrix[0][i];
                if (max_values[j] < current_value){
                    max_values[j] = current_value;
                    max_indices[j] = i;
                }
            }
        }
        for(float max_value:max_values){
            build_string += " "+max_value;
        }
        
        Matrix temp_delta = new Matrix(build_string);
        Matrix next_delta = temp_delta.multiplyElementwise(b_col);
        DeltaVariables return_variables = new DeltaVariables(next_delta, max_indices);
        return return_variables;
    }


    
}
class DeltaVariables{
    public Matrix delta;
    public int[] delta_idx;

    public DeltaVariables(Matrix in_delta, int[] in_delta_idx){
        delta = in_delta;
        delta_idx = in_delta_idx;
    }
}
