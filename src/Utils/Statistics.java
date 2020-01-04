package Utils;

import Math.Matrix;
import Math.Function;

public class Statistics {

    public static Matrix getAverage(Matrix data){
        /**
         * We take the average over the examples
         * of each feature. We get a horizontal
         * Matrix of dim(1, n) where each element
         * along n is the average of each feature
         * */
        Matrix avg = data.vSum();
        avg.elemOperation(1F/data.getM(), '*');
        return avg;
    }

    public static Matrix getSigma(Matrix data){
        /**
         * sigma = sqrt(SUM{x_i - x_mean}² / (n-1))
         * */
        Matrix data_cpy = new Matrix(data);
        // X - X_avg
        data_cpy.vBroadcast(Statistics.getAverage(data), '-');
        // SUM{(X - X_avg)²}
        data_cpy.selfMultMat(data_cpy);
        Matrix summed = data_cpy.vSum();
        // sqrt(SUM{(X - X_avg)²} / (n - 1))
        summed.elemOperation((data.getM()-1), '*');
        Function.sqrt(summed);
        return summed;
    }

    public static void normalize_standard(Matrix data){
        /**
         * Works well for populations that are normally distributed
         * standard score : X' = (X - µ) / sigma
         * */
        Matrix mu = Statistics.getAverage(data);
        Matrix sigma = Statistics.getSigma(data);
        data.vBroadcast(mu, '-');
        data.vBroadcast(sigma, '/');
    }

    public static void normalize_by_feature_min_max(Matrix data, float a, float b){
        /**
         * Min-Max feature scaling [0-1] : X' = (X - Xmin) / (Xmax - Xmin)
         * Generalized [a, b] : X' = a + (X - Xmin)(b - a) / (Xmax - Xmin)
         * */
        Matrix Xmax = data.max(1);
        Matrix Xmin = data.min(1);
        Matrix Xmax_sub_Xmin = Xmax.subMat(Xmin);
        if(a != 0 || b != 1){
            data.vBroadcast(Xmin, '-');
            data.elemOperation(b-a, '*');
            data.vBroadcast(Xmax_sub_Xmin, '/');
            data.elemOperation(a, '+');
        } else {
            data.vBroadcast(Xmin, '-');
            data.vBroadcast(Xmax_sub_Xmin, '/');
        }
    }

    /**
     * @param col_start from which col it will be normalized
     * @param col_end to which col it will be normalized
     *
     * @param r_min denotes the minimum of the range of the measurement
     * @param r_max denotes the maximum of the range of the measurement
     * @param t_min denotes the minimum of the range of the desired target scaling
     * @param t_max denotes the maximum of the range of the desired target scaling
     * @param data∈[rmin,rmax] denotes your measurement to be scaled
     * formula:
     *      data_scaled <- (data-r_min)/(r_max-r_min)*(t_max-t_min)+t_min
     * */
    public static void normalize_by_range(Matrix data, int col_start, int col_end, float r_min, float r_max, float t_min, float t_max){
        for(int row = 0; row < data.getM(); ++row){
            for(int col = col_start; col < col_end; ++col){
                float data_i_j = data.get(row, col);
                float data_i_j_normalized = (data_i_j-r_min)/(r_max-r_min)*(t_max-t_min)+t_min;
                data.set(row, col, data_i_j_normalized);
            }
        }
    }

    public static void normalize_min_max(Matrix data, float a, float b){
        /**
         * Min-Max feature scaling [0-1] : X' = (X - Xmin) / (Xmax - Xmin)
         * Generalized [a, b] : X' = a + (X - Xmin)(b - a) / (Xmax - Xmin)
         * */
        // get overall max/min
        float Xmax = data.max(-1).get(0, 0);
        float Xmin = data.min(-1).get(0, 0);
        Xmax = (Xmax == Xmin) ? ++Xmax : Xmax;
        float Xmax_sub_Xmin = Xmax - Xmin;
        if(a != 0 || b != 1){
            data.elemOperation(Xmin, '-');
            data.elemOperation((b-a)/Xmax_sub_Xmin, '*');
            data.elemOperation(a, '+');
        } else {
            data.elemOperation(Xmin, '-');
            data.elemOperation(1F/Xmax_sub_Xmin, '*');
        }
    }
    /**
     * @param data Matrix data that will be normalized by chunks
     * @param a min normalization value
     * @param b max normalization value
     * @param chunk_indices ranges of normalization (column indices)
     *                      let's say we want to normalize a matrix
     *                      where different chunks of the matrix are drawn from
     *                      a different distribution or are provided by different
     *                      sensors. We want to normalize these chunks separately.
     *                      The data matrix is truncated COLUMN-WISE
     *                      E.g.: normalize a matrix with 3 chunks. Give chunk param
     *                            the starting index of each chunk
     * */
    public static void normalize_min_max_by_chunks(Matrix data, float a, float b, int... chunk_indices){
        for(int c = 0; c < chunk_indices.length-1; ++c){
            int from_col = chunk_indices[c];
            int to_col = chunk_indices[c+1];
            Matrix truncated_matrix = new Matrix(data.getM(), to_col-from_col);
            truncated_matrix.copy_slice(data, 0, from_col);
            // we normalize only the truncated matrix
            Statistics.normalize_min_max(truncated_matrix, a, b);
            // we update data
            data.copy_in_range(truncated_matrix, 0, from_col);
        }
    }

    /**
     * Converts each digit of a dataset into a one-hot vector
     * which dimension is based on the absolute difference
     * in between max and min of the dataset.
     * E.g.: if dataset consists of integers [-2, 10]
     * -> |10 - (-2)| + 1 = 13 -> 13 classes
     * @param data : vertical vector of digits
     * @return res : each line -> one-hot vector
     */
    public static Matrix digits_matrix_to_one_hot_matrix(Matrix data){
        float min = data.min(-1).get(0, 0);
        float max = data.max(-1).get(0, 0);
        int one_hot_length = (int)Math.abs(max - min) + 1;
        Matrix res = new Matrix(data.getM(), one_hot_length);

        for(int i = 0; i < data.getM(); ++i){
            // we need to add |min| to get indexes >= 0
            res.set(i, (int)(data.get(i, 0)+Math.abs(min)), 1F);
        }
        return res;
    }

    /**
     * Converts each digit of a dataset into a one-hot vector
     * which dimension is based on the absolute difference
     * in between max and min of the dataset.
     * E.g.: if dataset consists of integers [-2, 10]
     * -> |10 - (-2)| + 1 = 13 -> 13 classes
     * @param data : vertical vector of digits
     * @return res : each line -> one-hot vector
     */
    public static Matrix digits_matrix_to_one_hot_matrix_n_classes(Matrix data, int classes){
        Matrix res = new Matrix(data.getM(), classes);

        for(int i = 0; i < data.getM(); ++i){
            // we need to add |min| to get indexes >= 0
            res.set(i, (int)data.get(i, 0), 1F);
        }
        return res;
    }

    /**
     * @param data Matrix type data that will be filtered and not overriden
     * @param col_start col index from which we do the filtering
     * @param col_end col index at which we stop doing the filtering
     * @return res filtered data
     * Kalman filter:
     *      k = p_t / (p_t + r)
     *      x_t = x_{t-1} + k * (m - x_{t-1})
     *      p_t = (1 - k) * p_{t-1}
     * */
    public static Matrix kalman_filter_by_column(Matrix data, int col_start, int col_end){
        // the slice of interest in data
        Matrix slice_mat = new Matrix(data.getM(), col_end-col_start+1);
        slice_mat.copy_slice(data, 0, col_start);
        Matrix avg = Statistics.getAverage(slice_mat);
        Matrix noise = Statistics.getSigma(slice_mat);

        for(int j = col_start; j < col_end+1; ++j){
            // initial estimate
            float x = avg.get(0, j-col_start);
            // initial error
            float p = Math.abs(x-slice_mat.get(0, j-col_start));
            // noise
            float r = noise.get(0, j-col_start);
            // kalman gain
            float k = 0;
            for(int i = 0; i < data.getM(); ++i){
                k = p / (p + r);
                float m = slice_mat.get(i, j-col_start);
                x = x + k * (m - x);
                p = (1 - k) * p;
                slice_mat.set(i, j-col_start, x);
            }
        }
        return slice_mat;
    }

    /**
     * clipping values of data between 2 param
     * @param inf
     * @param sup
     * */
    public static void clip(Matrix data, float inf, float sup){
        float curr_val, clipped_val;
        for(int i = 0; i < data.getM(); ++i){
            for(int j = 0; j < data.getN(); ++j){
                curr_val = data.get(i, j);
                clipped_val = Math.max(sup, curr_val);
                clipped_val = Math.min(inf, clipped_val);
                data.set(i, j, clipped_val);
            }
        }
    }

    public static Matrix getVMR(Matrix data){
        /**
         * Variance to Mean Ratio (VMR) measures whether
         * some data is clustered (not dispersed) or not
         * generally used for positive data. User may find
         * useful to normalize [0, 1] before using VMR
         *
         * |   Distribution	                |   VMR	      |   interpretation
         * -----------------------------------------------------------------
         * | constant random variable	    |   VMR = 0	  | not dispersed
         * | binomial distribution	        | 0 < VMR < 1 | under-dispersed
         * | Poisson distribution	        |   VMR = 1	  |
         * | negative binomial distribution |	VMR > 1	  | over-dispersed
         *
         * D = sigma² / µ
         * */
        Matrix sigma = Statistics.getSigma(data);
        Matrix mu = Statistics.getAverage(data);
        Function.pow(sigma, 2);
        sigma.selfDivMat(mu);
        return sigma;
    }
}
