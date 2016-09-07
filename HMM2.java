import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class HMM2 {


    public static void main(String[] args) {
    	String file_location = "D:/Felix/Programming/Workspace/HMM2/data/sample00";
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
        int[] o = readInitialState(data.nextLine());
        data.close();

        Matrix[] alpha_container = new Matrix[o.length];
        alpha_container[0] = pi.multiplyElementwise(b.getColumn(o[0])); /*initialize alpha 1*/
        for (int i = 1; i<o.length; i++){
            Matrix current_b_column = b.getColumn(o[i]);
            alpha_container[i] = alpha_container[i-1].multiply(a).multiplyElementwise(current_b_column);
        }
        alpha_container[alpha_container.length-1].printSum();
    }

    public static int[] readInitialState(String inStringMatrix){
        String[] listMatrix=inStringMatrix.split(" ");
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
    private final String stringMatrix;
    
    public Matrix(String inStringMatrix){
        String[] listMatrix=inStringMatrix.split(" ");
        rows=Integer.valueOf(listMatrix[0]);
        columns=Integer.valueOf(listMatrix[1]);
        int counter=2;
        stringMatrix=inStringMatrix;
        matrix=new float[rows][columns];
        for(int j=0;j<rows;j++){
            for(int k=0;k<columns;k++){
                matrix[j][k]=Float.valueOf(listMatrix[counter]);
                counter++;
            }
        }
    }
    public Matrix multiply(Matrix inMatrix){//returns Matrix*inMatrix
      String buildString=rows+ " " + inMatrix.columns;
        for(int h=0;h<inMatrix.columns;h++){
            for(int i=0; i<rows;i++){
            float tmpSum=0;
                for(int j=0;j<columns;j++){
                    tmpSum=tmpSum+inMatrix.matrix[j][h]*matrix[i][j];
                }
            buildString=buildString+" "+ tmpSum;
            }  
        }
      return new Matrix(buildString);
    }
    public Matrix multiplyElementwise(Matrix inMatrix){
        /*Returns matrix.*inMatrix as a row vector. Both matrix and inMatrix need to be row vectors of same dimension*/
        String buildString=rows+ " " + columns;
        float element_product;
        for(int j = 0; j<columns; j++){
            element_product = matrix[0][j]*inMatrix.matrix[0][j];
            buildString=buildString+" "+ Float.toString(element_product);
        }
        return new Matrix(buildString);

    }

    public String string(){
        return stringMatrix;
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
        String buildString=Integer.toString(1)+ " " + Integer.toString(rows);
        for(float[] row:matrix){
            buildString = buildString + " " +row[column_idx];
        }
        return new Matrix(buildString);
    }
}