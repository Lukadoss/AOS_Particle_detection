package methods;

abstract class EdgeMath {
    int truncate(int a) {
        if      (a <   0) return 0;
        else if (a > 255) return 255;
        else              return a;
    }

    int[][] rotateConv(int[][] conv1, int dir) {
        dir = 8-dir;
        for (int i = dir;i>0 && i<8;i--){
            conv1 = rotateMatrix(conv1);
        }
        return conv1;
    }

    private int[][] rotateMatrix(int[][] mat) {
        return rotateMatrix(3,3, mat);
    }

    private int[][] rotateMatrix(int m, int n, int[][] mat) {
        int row = 0, col = 0;
        int prev, curr;

        while (row < m && col < n)
        {
            if (row + 1 == m || col + 1 == n)
                break;

            prev = mat[row + 1][col];

            for (int i = col; i < n; i++)
            {
                curr = mat[row][i];
                mat[row][i] = prev;
                prev = curr;
            }
            row++;

            for (int i = row; i < m; i++)
            {
                curr = mat[i][n-1];
                mat[i][n-1] = prev;
                prev = curr;
            }
            n--;

            if (row < m)
            {
                for (int i = n-1; i >= col; i--)
                {
                    curr = mat[m-1][i];
                    mat[m-1][i] = prev;
                    prev = curr;
                }
            }
            m--;

            if (col < n)
            {
                for (int i = m-1; i >= row; i--)
                {
                    curr = mat[i][col];
                    mat[i][col] = prev;
                    prev = curr;
                }
            }
            col++;
        }

//        for (int i=0;i<3;i++){
//            for (int j=0;j<3;j++){
//                System.out.print(mat[i][j]+" ");
//            }
//            System.out.println();
//        }

        return mat;
    }
}
