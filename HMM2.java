import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class HMM2 {


    public static void main(String[] args) {
        String file_location = "D:/Felix/Programming/Workspace/HMM2/data/sample00";
        boolean use_std_input = false;
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

        Matrix[] alpha_container = new Matrix[obs_seq.length];
        alpha_container[0] = pi.multiplyElementwise(b.getColumn(obs_seq[0])); /*initialize alpha 1*/
        for (int i = 1; i<obs_seq.length; i++){
            Matrix current_b_column = b.getColumn(obs_seq[i]);
            alpha_container[i] = (alpha_container[i-1].multiply(a)).multiplyElementwise(current_b_column);
        }
        alpha_container[alpha_container.length-1].printSum();
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
    private final float[][] matrix;
    private final int rows;
    private final int columns;
    private final String string_matrix;
    
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
    public void printSum(){
        /*Prints the sum of the row elements in a row vector*/
        float sum = 0;
        for(float elem:matrix[0]){
            sum += elem;
        }
        System.out.println(sum);
    }
    public Matrix getColumn(int column_idx){
        /*Returns the column of a given matrix at a given index as a row vector matrix*/
        String build_string=Integer.toString(1)+ " " + Integer.toString(rows);
        for(float[] row:matrix){
            build_string = build_string + " " +row[column_idx];
        }
        return new Matrix(build_string);
    }
}