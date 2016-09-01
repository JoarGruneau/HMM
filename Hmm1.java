import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author joar
 */
public class Hmm1 {


    public static void main(String[] args) {
        //readAndSolve();
        Matrix a=new Matrix("4 4 0.2 0.5 0.3 0.0 0.1 0.4 0.4 0.1 0.2 0.0 0.4 0.4 0.2 0.3 0.0 0.5");
        Matrix b=new Matrix("4 3 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 0.2 0.6 0.2");
        Matrix pi=new Matrix("1 4 0.0 0.0 0.0 1.0");
        Matrix piA=pi.multiply(a);
        Matrix piAB=piA.multiply(b);
        System.out.println(piAB.string());
    }
    public static void readAndSolve(){
        Scanner data=new Scanner(System.in);
        Matrix a=new Matrix(data.nextLine());
        Matrix b=new Matrix(data.nextLine());
        Matrix pi=new Matrix(data.nextLine());
        data.close();
        System.out.println(pi.multiply(a).multiply(b).string());
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
        stringMatrix=inStringMatrix;
        matrix=new float[rows][columns];
        int counter=2;
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
      
      Matrix returnMatrix=new Matrix(buildString);
      return returnMatrix;
    }
    public String string(){
        return stringMatrix;
    }
    public void print(){
        for(float[] row: matrix){
            System.out.println(Arrays.toString(row));
        }
    }
}
