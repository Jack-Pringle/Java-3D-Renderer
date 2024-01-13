public class Matrix3x3 {
    
    private
        double[][] matrix;

    public
        Matrix3x3() {}

        Matrix3x3(double[][] matrix) {
            this.matrix = matrix;
        }


        void setMatrix(double[][] matrix) {
            this.matrix = matrix;
        }

        double[][] getMatrix() {
            return matrix;
        }


        Node multiplyPoint(Node point) {
            return new Node(
                point.getX() * matrix[0][0] + point.getY() * matrix[0][1] + point.getZ() * matrix[0][2],
                point.getX() * matrix[1][0] + point.getY() * matrix[1][1] + point.getZ() * matrix[1][2], 
                point.getX() * matrix[2][0] + point.getY() * matrix[2][1] + point.getZ() * matrix[2][2]
            );
        }

        Matrix3x3 multiply3x3(Matrix3x3 otherMatrix) {
            double[][] resultMatrix = new double[3][3];

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    resultMatrix[i][j] = 0;
                    for (int k = 0; k < 3; k++) {
                        resultMatrix[i][j] += matrix[i][k] * otherMatrix.matrix[k][j];
                    }
                }
            }

            return new Matrix3x3(resultMatrix);
        }

}
