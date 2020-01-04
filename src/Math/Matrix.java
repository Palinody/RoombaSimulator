package Math;

import java.util.*;

import Streams.Streams;
import Utils.PRNG;

public class Matrix extends Streams {

    public Matrix(){
    }
    // cpy constructor
    public Matrix(float[] data, int m, int n){
        this._m = m;
        this._n = n;
        this._f_matrix = data.clone();
    }
    // cpy constructor
    public Matrix(Matrix other){
        this._m = other._m;
        this._n = other._n;
        this._f_matrix = other._f_matrix.clone();
    }
    // obj init with data from binary file
    public Matrix(String from_file){
        super(from_file);
    }

    public Matrix(int m, int n){
        //_f_matrix = new ArrayList(m*n);
        this._m = m;
        this._n = n;
        this._f_matrix = new float[m*n];
    }

    public Matrix(int m, int n, float value){
        //_f_matrix = new ArrayList(m*n);
        this._m = m;
        this._n = n;
        this._f_matrix = new float[m*n];
        for(int i = 0; i < m; ++i){
            for(int j  = 0; j < n; ++j){
                this._f_matrix[j + i * n] = value;
            }
        }
    }

    public Matrix(int m, int n, String distr){
        this._m = m;
        this._n = n;
        this._f_matrix = new float[m*n];

        PRNG prng = new PRNG();

        if(distr.equals("gauss")){
            for(int i = 0; i < m; ++i){
                for(int j  = 0; j < n; ++j){
                    this._f_matrix[j + i * n] = prng.gaussian(0, 1F);
                }
            }
        } else if (distr.equals("xavier")) {
            for (int i = 0; i < m; ++i) {
                for (int j = 0; j < n; ++j) {
                    this._f_matrix[j + i * n] = prng.xavier(_m, _n);
                }
            }
        } else if (distr.equals("uniform")) {
            for (int i = 0; i < m; ++i) {
                for (int j = 0; j < n; ++j) {
                    this._f_matrix[j + i * n] = prng.uniform(0, 1);
                }
            }
        } else if (distr.equals("integer")) {
            for (int i = 0; i < m; ++i) {
                for (int j = 0; j < n; ++j) {
                    this._f_matrix[j + i * n] = prng.randInt(0, 1);
                }
            }
        } else { // default is N(0, 1)
            for(int i = 0; i < m; ++i){
                for(int j  = 0; j < n; ++j){
                    this._f_matrix[j + i * n] = prng.gaussian(0, 1);
                }
            }
        }
    }

    ///////////////////////////////////////////////
    //              GETTER/SETTER                //
    ///////////////////////////////////////////////

    public float get(int i, int j){
        return this._f_matrix[j + i * this._n];
    }
    public int getM(){ return this._m; }
    public int getN(){ return this._n; }
    public int getSize(){ return this._m * this._n; }

    public void set(int i, int j, float value){
        this._f_matrix[j + i * this._n] = value;
    }

    ///////////////////////////////////////////////
    //                  METHODS                  //
    ///////////////////////////////////////////////

    // copies data from rhs to this (must have same dimensions)
    public void copy(Matrix rhs){
        for(int i = 0; i < this._m; ++i){
            for(int j = 0; j < this._n; ++j){
                this._f_matrix[j + i * this._n] = rhs.get(i, j);
            }
        }
    }

    /**
     * Copies data from a slice of rhs to this.
     * User must be cautious with dimensions.
     * to_i-from_i and to_j_from_j are defined by
     * this._m resp. this._n
     * @param from_i : start parsing from line
     * @param from_j : start parsing from column
     * @return None : this._f_matrix directly modified
    * */
    public void copy_slice(Matrix rhs, int from_i, int from_j){
        try{
            for(int i = 0; i < this._m; ++i){
                for(int j = 0; j < this._n; ++j){
                    this._f_matrix[j + i * this._n] = rhs.get(from_i+i, from_j+j);
                }
            }
        } catch (Exception e){ e.printStackTrace(); }
    }
    // copies data from vector (requirement : m = this._m; n = this._n;)
    public void copy(float[] rhs, int m, int n){
        for(int i = 0; i < this._m; ++i){
            for(int j = 0; j < this._n; ++j){
                this._f_matrix[j + i * this._n] = rhs[j + i * this._n];
            }
        }
    }

    /**
     * copies rhs into a specified part of this
     * @param rhs right hand side Matrix
     * @param from_i row index of this
     * @param from_j col index of this
     */
    public void copy_in_range(Matrix rhs, int from_i, int from_j){
        for(int i = 0; i < rhs.getM(); ++i){
            for(int j = 0; j < rhs.getN(); ++j){
                this._f_matrix[(from_j +j ) + from_i + i * this._n] = rhs.get(i, j);
            }
        }
    }

    // sorts the matrix row-wise and returns a new vector of size m*n
    public float[] sort(){
        float[] sorted_array = this._f_matrix.clone();
        Arrays.sort(sorted_array);
        return sorted_array;
    }

    public Matrix T(){
        Matrix res = new Matrix(this._n, this._m);
        for(int i = 0; i < this._m; ++i){
            for(int j = 0; j < this._n; ++j){
                res.set(j, i, this._f_matrix[j + i * this._n]);
            }
        }
        return res;
    }
    // sums a matrix vertically until it shrinks to a single horizontal vector
    public Matrix vSum(){
        Matrix res = new Matrix(1, this._n);
        for(int j = 0; j < this._n; ++j){
            float temp = 0;
            for(int i = 0; i < this._m; ++i){
                temp += this._f_matrix[j + i * this._n];
            }
            res.set(0, j, temp);
        }
        return res;
    }
    // sums a matrix horizontally until it shrinks to a single vertical vector
    public Matrix hSum(){
        Matrix res = new Matrix(this._m, 1);
        for(int i = 0; i < this._m; ++i){
            float temp = 0;
            for(int j = 0; j < this._n; ++j){
                temp += this._f_matrix[j + i * this._n];
            }
            res.set(i, 0, temp);
        }
        return res;
    }

    public Matrix max(int axes) {
        /*
         * axes = 0 : row-wise max of matrix
         *      output -> mx1 matrix
         * axes = 1 : column-wise max of matrix
         *      output -> 1xn matrix
         * else : max element of matrix
         *      output -> 1x1 matrix
         * */
        float curr_max;

        if (axes == 0) {
            Matrix res = new Matrix(this._m, 1);
            for (int i = 0; i < this._m; ++i) {
                curr_max = -Float.MAX_VALUE;
                for (int j = 0; j < this._n; ++j) {
                    float curr_val = this._f_matrix[j + i * this._n];
                    curr_max = Math.max(curr_val, curr_max);
                }
                res.set(i, 0, curr_max);
            }
            return res;
        } else if (axes == 1) {
            Matrix res = new Matrix(1, this._n);
            for (int j = 0; j < this._n; ++j) {
                curr_max = -Float.MAX_VALUE;
                for (int i = 0; i < this._m; ++i) {
                    float curr_val = this._f_matrix[j + i * this._n];
                    curr_max = Math.max(curr_val, curr_max);
                }
                res.set(0, j, curr_max);
            }
            return res;
        } else {
            curr_max = -Float.MAX_VALUE;
            for (int i = 0; i < this._m; ++i) {
                for (int j = 0; j < this._n; ++j) {
                    float curr_val = this._f_matrix[j + i * this._n];
                    curr_max = Math.max(curr_val, curr_max);
                }
            }
            return new Matrix(1, 1, curr_max);
        }
    }

    public Matrix min(int axes) {
        /*
         * axes = 0 : row-wise min of matrix
         *      output -> mx1 matrix
         * axes = 1 : column-wise min of matrix
         *      output -> 1xn matrix
         * else : min element of matrix
         *      output -> 1x1 matrix
         * */
        float curr_min;

        if (axes == 0) {
            Matrix res = new Matrix(this._m, 1);
                for (int i = 0; i < this._m; ++i) {
                    curr_min = Float.MAX_VALUE;
                    for (int j = 0; j < this._n; ++j) {
                        float curr_val = this._f_matrix[j + i * this._n];
                        curr_min = Math.min(curr_val, curr_min);
                    }
                    res.set(i, 0, curr_min);
            }
            return res;
        } else if (axes == 1) {
            Matrix res = new Matrix(1, this._n);
            for (int j = 0; j < this._n; ++j) {
                curr_min = Float.MAX_VALUE;
                for (int i = 0; i < this._m; ++i) {
                    float curr_val = this._f_matrix[j + i * this._n];
                    curr_min = Math.min(curr_val, curr_min);
                }
                res.set(0, j, curr_min);
            }
            return res;
        } else {
            curr_min = Float.MAX_VALUE;
            for (int i = 0; i < this._m; ++i) {
                for (int j = 0; j < this._n; ++j) {
                    float curr_val = this._f_matrix[j + i * this._n];
                    curr_min = Math.min(curr_val, curr_min);
                }
            }
            return new Matrix(1, 1, curr_min);
        }
    }
    // writes matrix content in binary format
    void writeBinary(String to_path){
        this.write_buff(to_path);
    }

    public Matrix indexOfMax(int axes){
        /*
         * axes = 0 : row-wise index of max of matrix
         *      output -> mx1 matrix
         * axes = 1 : column-wise index of max of matrix
         *      output -> 1xn matrix
         * else returns a 1x2 matrix containing i j coord.
         * of the overall max value
         * */
        float curr_max;
        int curr_max_index = -1;

        if(axes == 0) {
            Matrix res = new Matrix(this._m, 1);
            for (int i = 0; i < this._m; ++i) {
                curr_max = -Float.MAX_VALUE;
                for (int j = 0; j < this._n; ++j) {
                    float curr_val = this._f_matrix[j + i * this._n];
                    if (curr_val > curr_max) {
                        curr_max = curr_val;
                        curr_max_index = j;
                    }
                }
                res.set(i, 0, (float) curr_max_index);
            }
            return res;
        }else if(axes == 1){
                Matrix res = new Matrix(1, this._n);
                for (int j = 0; j < this._n; ++j) {
                    curr_max = -Float.MAX_VALUE;
                    for (int i = 0; i < this._m; ++i) {
                        float curr_val = this._f_matrix[j + i * this._n];
                        if(curr_val > curr_max){
                            curr_max = curr_val;
                            curr_max_index = i;
                        }
                    }
                    res.set(0, j, (float)curr_max_index);
            }
            return res;
        } else {
            curr_max = -Float.MAX_VALUE;
            int curr_max_i = -1;
            int curr_max_j = -1;
            for (int i = 0; i < this._m; ++i) {
                for (int j = 0; j < this._n; ++j) {
                    float curr_val = this._f_matrix[j + i * this._n];
                    if(curr_val > curr_max){
                        curr_max = curr_val;
                        curr_max_i = i;
                        curr_max_j = j;
                    }
                }
            }
            Matrix res = new Matrix(1, 2);
            res.set(0, 0, curr_max_i);
            res.set(0, 1, curr_max_j);
            return res;

        }
    }

    public boolean isEqual(Matrix rhs){
        for(int i = 0; i < this._m; ++i){
            for(int j = 0; j < this._n; ++j){
                float value_rhs = rhs.get(i, j);
                if(value_rhs != this._f_matrix[j + i * this._n]){
                    return false;
                }
            }
        }
        return true;
    }

    public Matrix isEqualMatrix(Matrix rhs){
        Matrix res = new Matrix(this._m, this._n);
        for(int i = 0; i < this._m; ++i){
            for(int j = 0; j < this._n; ++j){
                float value_rhs = rhs.get(i, j);
                if(value_rhs == this._f_matrix[j + i * this._n]){
                    res.set(i, j, 1F);
                }
            }
        }
        return res;
    }

    public Matrix concatenate(Matrix other){
        /*
         * axes = 0 : adds a row to matrix
         * axes = 1 : adds a column (NOT IMPLEMENTED YET)
         * */
        Matrix res = new Matrix(this._m+other.getM(), this._n);
        for(int this_i = 0; this_i < this._m; ++this_i){
            for(int this_j = 0; this_j < this._n; ++this_j){
                res.set(this_i, this_j, this._f_matrix[this_j + this_i * this._n]);
            }
        }
        for(int other_i = 0; other_i < other.getM(); ++other_i){
            for(int other_j = 0; other_j < other.getN(); ++other_j){
                res.set(this._m+other_i, other_j, other.get(other_i, other_j));
            }
        }
        return res;
    }

    public Matrix vShuffle(){
        Matrix res = new Matrix(this._m, this._n);
        // START: create array of indices
        int[] indices = new int[this._m];
        for(int i = 0; i < this._m; ++i){
            indices[i] = i;
        }
        // END: create array of indices
        // START: create vector of shuffled indices of this._m length
        Random rand = new Random();
        for(int i = 0; i < this._m; ++i){
            int randIndexToSwap = rand.nextInt(indices.length);
            int temp = indices[randIndexToSwap];
            indices[randIndexToSwap] = indices[i];
            indices[i] = temp;
        }
        // END: create vector of shuffled indices of this._m length
        for(int i = 0; i < this._m; ++i){
            for(int j = 0; j < this._n; ++j){
                res.set(i, j, this._f_matrix[j + indices[i] * this._n]);
            }
        }
        return res;
    }

    ///////////////////////////////////////////////
    //      OPERATORS MATRIX - MATRIX            //
    ///////////////////////////////////////////////

    public Matrix dotProd(Matrix rhs){
        int new_m = this._m;
        int new_n = rhs.getN();
        Matrix res = new Matrix(new_m, new_n);

        float temp = 0;
        for(int i = 0; i < new_m; ++i){
            for(int j = 0; j < new_n; ++j){
                for(int k = 0; k < this._n; ++k){
                    temp += this._f_matrix[k + i * this._n] * rhs.get(k, j);
                }
                res.set(i, j, temp);
                temp = 0;
            }
        }
        return res;
    }

    public Matrix dotProdTranspose(Matrix rhs){
        /*
         * computes M * rhs.T without explicitly doing the transpose of rhs
         */
        int new_m = this._m;
        int new_n = rhs.getM(); // because we take the transpose of rhs
        Matrix res = new Matrix(new_m, new_n);

        float temp = 0;
        for(int i = 0; i < new_m; ++i){
            for(int j = 0; j < new_n; ++j){
                for(int k = 0; k < this._n; ++k){
                    temp += this._f_matrix[k + i * this._n] * rhs.get(j, k);
                }
                res.set(i, j, temp);
                temp = 0;
            }
        }
        return res;
    }

    public Matrix transposeDotProd(Matrix rhs){
        /*
         * computes M.T * rhs without explicitly doing the transpose of M
         */
        int new_m = this._n; // because we take the transpose of M
        int new_n = rhs.getN();
        Matrix res = new Matrix(new_m, new_n);

        float temp = 0;
        for(int i = 0; i < new_m; ++i){
            for(int j = 0; j < new_n; ++j){
                for(int k = 0; k < this._m; ++k){
                    temp += this._f_matrix[i + k * this._n] * rhs.get(k, j);
                }
                res.set(i, j, temp);
                temp = 0;
            }
        }
        return res;
    }

    public Matrix addMat(Matrix rhs){
        /*
         * make sure dimensions are correct
         * will maybe add some error handling
         * */
        Matrix new_f_matrix = new Matrix(this._m, this._n);
        for(int i = 0; i < this._m; ++i){
            for(int j = 0; j < this._n; ++j){
                float value = this._f_matrix[j + i * this._n] + rhs.get(i, j);
                new_f_matrix.set(i, j, value);
            }
        }
        return new_f_matrix;
    }

    public Matrix subMat(Matrix rhs){
        /*
         * make sure dimensions are correct
         * will maybe add some error handling
         * */
        Matrix new_f_matrix = new Matrix(this._m, this._n);
        for(int i = 0; i < this._m; ++i){
            for(int j = 0; j < this._n; ++j){
                float value = this._f_matrix[j + i * this._n] - rhs.get(i, j);
                new_f_matrix.set(i, j, value);
            }
        }
        return new_f_matrix;
    }
    // element-wise multiplication
    public Matrix multMat(Matrix rhs){
        /*
         * make sure dimensions are correct
         * will maybe add some error handling
         * */
        Matrix new_f_matrix = new Matrix(this._m, this._n);
        for(int i = 0; i < this._m; ++i){
            for(int j = 0; j < this._n; ++j){
                float value = this._f_matrix[j + i * this._n] * rhs.get(i, j);
                new_f_matrix.set(i, j, value);
            }
        }
        return new_f_matrix;
    }
    // element-wise division
    public Matrix divMat(Matrix rhs){
        /*
         * make sure dimensions are correct
         * will maybe add some error handling
         * */
        Matrix new_f_matrix = new Matrix(this._m, this._n);
        for(int i = 0; i < this._m; ++i){
            for(int j = 0; j < this._n; ++j){
                float value = this._f_matrix[j + i * this._n] / (rhs.get(i, j) + 1e-8F);
                new_f_matrix.set(i, j, value);
            }
        }
        return new_f_matrix;
    }
    // element-wise addition (SELF)
    public void selfAddMat(Matrix rhs){
        /*
         * make sure dimensions are correct
         * will maybe add some error handling
         * */
        for(int i = 0; i < this._m; ++i){
            for(int j = 0; j < this._n; ++j){
                this._f_matrix[j + i * this._n] += rhs.get(i, j);
            }
        }
    }
    // element-wise subtraction (SELF)
    public void selfSubMat(Matrix rhs){
        /*
         * make sure dimensions are correct
         * will maybe add some error handling
         * */
        for(int i = 0; i < this._m; ++i){
            for(int j = 0; j < this._n; ++j){
                this._f_matrix[j + i * this._n] -= rhs.get(i, j);
            }
        }
    }
    // element-wise multiplication (SELF)
    public void selfMultMat(Matrix rhs){
        /*
         * make sure dimensions are correct
         * will maybe add some error handling
         * */
        for(int i = 0; i < this._m; ++i){
            for(int j = 0; j < this._n; ++j){
                this._f_matrix[j + i * this._n] *= rhs.get(i, j);
            }
        }
    }
    // element-wise division (SELF)
    public void selfDivMat(Matrix rhs){
        /*
         * make sure dimensions are correct
         * will maybe add some error handling
         * */
        for(int i = 0; i < this._m; ++i){
            for(int j = 0; j < this._n; ++j){
                this._f_matrix[j + i * this._n] /= (rhs.get(i, j) + 1e-8F);
            }
        }
    }

    ///////////////////////////////////////////////
    //      OPERATORS MATRIX - VECTOR            //
    ///////////////////////////////////////////////

    // performs horizontal broadcasting.
    // Resulting matrix takes the one with higher dimensions
    public void hBroadcast(Matrix rhs, char operator){
        /*
         * make sure dimensions are correct
         * will maybe add some error handling
         *
         * other MUST be a Matrix<T> with one of its dimensions being one
         * [[0, 1, 2, 3],     [[ 2],     [[0 * 2, 1 * 2, 2 * 2, 3 * 2],
         *  [2,-1,-2, 1]]  *   [-3]]  ->  [2 *-3,-1 *-3,-2 *-3, 1 *-3]]
         *
         * */
        if(operator == '+'){
            for(int i = 0; i < this._m; ++i){
                for(int j = 0; j < this._n; ++j){
                    this._f_matrix[j + i * this._n] += rhs.get(i, 0);
                }
            }
        } else if(operator == '-'){
            for(int i = 0; i < this._m; ++i){
                for(int j = 0; j < this._n; ++j){
                    this._f_matrix[j + i * this._n] -= rhs.get(i, 0);
                }
            }
        } else if(operator == '*'){
            for(int i = 0; i < this._m; ++i){
                for(int j = 0; j < this._n; ++j){
                    this._f_matrix[j + i * this._n] *= rhs.get(i, 0);
                }
            }
        } else if(operator == '/'){
            for(int i = 0; i < this._m; ++i){
                for(int j = 0; j < this._n; ++j){
                    this._f_matrix[j + i * this._n] /= (rhs.get(i, 0) + 1e-8F);
                }
            }
        }
    }
    // performs horizontal broadcasting.
    // Resulting matrix dimensions takes the ones with higher dimensions
    public void vBroadcast(Matrix rhs, char operator){
        /*
         * make sure dimensions are correct
         * will maybe add some error handling
         *
         * other MUST be a Matrix<T> with one of its dimensions being one
         * [[0, 1, 2, 3],                        [[0 * 4, 1 * 5, 2 * 3, 3 * 1],
         *  [2,-1,-2, 1]]  *  [[4, 5, 3, 1]]  ->  [2 * 4,-1 * 5,-2 * 3, 1
         *
         * */
        if(operator == '+'){
            for(int i = 0; i < this._m; ++i){
                for(int j = 0; j < this._n; ++j){
                    this._f_matrix[j + i * this._n] += rhs.get(0, j);
                }
            }
        } else if(operator == '-'){
            for(int i = 0; i < this._m; ++i){
                for(int j = 0; j < this._n; ++j){
                    this._f_matrix[j + i * this._n] -= rhs.get(0, j);
                }
            }
        } else if(operator == '*'){
            for(int i = 0; i < this._m; ++i){
                for(int j = 0; j < this._n; ++j){
                    this._f_matrix[j + i * this._n] *= rhs.get(0, j);
                }
            }
        } else if(operator == '/'){
            for(int i = 0; i < this._m; ++i){
                for(int j = 0; j < this._n; ++j){
                    this._f_matrix[j + i * this._n] /= (rhs.get(0, j) + 1e-8F);
                }
            }
        }
    }

    ///////////////////////////////////////////////
    //      OPERATORS MATRIX - ELEMENT           //
    ///////////////////////////////////////////////

    public Matrix elemOperation(float rhs, char operator){
        /*
         * make sure dimensions are correct
         * will maybe add some error handling
         * */
        if(operator == '+'){
            for(int i = 0; i < this._m; ++i){
                for(int j = 0; j < this._n; ++j){
                    this._f_matrix[j + i * this._n] += rhs;
                }
            }
        } else if(operator == '-'){
            for(int i = 0; i < this._m; ++i){
                for(int j = 0; j < this._n; ++j){
                    this._f_matrix[j + i * this._n] -= rhs;
                }
            }
        } else if(operator == '*'){
            for(int i = 0; i < this._m; ++i){
                for(int j = 0; j < this._n; ++j){
                    this._f_matrix[j + i * this._n] *= rhs;
                }
            }
        } else if(operator == '/'){
            for(int i = 0; i < this._m; ++i){
                for(int j = 0; j < this._n; ++j){
                    this._f_matrix[j + i * this._n] /= (rhs + 1e-8F);
                }
            }
        }
        return this;
    }

    public Matrix elemLeftOperation(float lhs, char operator){
        /*
         * make sure dimensions are correct
         * will maybe add some error handling
         * */
        if(operator == '+'){
            this.elemOperation(lhs, operator);
        } else if(operator == '-'){
            for(int i = 0; i < this._m; ++i){
                for(int j = 0; j < this._n; ++j){
                    this._f_matrix[j + i * this._n] = lhs - this._f_matrix[j + i * this._n];
                }
            }
        } else if(operator == '*'){
            this.elemOperation(lhs, operator);
        } else if(operator == '/'){
            for(int i = 0; i < this._m; ++i){
                for(int j = 0; j < this._n; ++j){
                    this._f_matrix[j + i * this._n] = (lhs + 1e-8F) / this._f_matrix[j + i * this._n];
                }
            }
        }
        return this;
    }

    ///////////////////////////////////////////////
    //              VISUALIZATION                //
    ///////////////////////////////////////////////

    // Data visualization tool
    public void printDistr(int width, float scale){
        /*
         * Helps to visualize the distribution of our sorted data
         * Example for a xavier distribution with 0.2F sampling
         *
         * --> (2)
         * ---> (3)
         * -------------> (13)
         * ----------------------> (22)
         * ------------------------------> (30)
         * ------------------> (18)
         * -------> (7)
         * ----> (4)
         * -> (1)
         * */
        float[] sorted_array = this.sort();
        // each element of the frequency distribution will be a counter
        // of the numbers in the range of the current sampling
        ArrayList<Integer> freq_distr = new ArrayList<Integer>();
        ArrayList<Float> ticks = new ArrayList<Float>();

        float inf = sorted_array[0];
        float sample = (sorted_array[sorted_array.length-1] - inf) / (float)width;
        float sup = inf + sample;
        int counter = 0;
        freq_distr.add(counter);
        ticks.add(inf);

        for(int i = 0; i < this._m*this._n; ++i){
            if(sorted_array[i] >= inf && sorted_array[i] < sup){
                int last_index = freq_distr.size() - 1;
                int prev = freq_distr.get(last_index);
                freq_distr.set(last_index, prev+1);
                ++counter;
            } else {
                counter = 0;
                inf = sup;
                sup = inf + sample;
                freq_distr.add(counter);
                ticks.add(inf);
            }
        }
        for(int i = 0; i < freq_distr.size(); ++i){
            int occurrences = freq_distr.get(i);
            float first = ticks.get(i);
            float second = ticks.get(i)+sample;
            if(first < 0 && second < 0){
                System.out.printf("[%3.2f;%3.2f]", first, second);
            } else if(first < 0 && second >= 0){
                System.out.printf("[%3.2f; %3.2f]", first, second);
            } else {
                System.out.printf("[ %3.2f; %3.2f]", first, second);
            }

            for(int occ = 0; occ < (int)(occurrences * scale); ++occ){
                System.out.printf("-");
            }
            System.out.printf("> (%d)\n", occurrences);
        }
        System.out.println();
    }

    public void print(){
        System.out.println("-----");
        for(int i = 0; i < this._m; ++i) {
            for (int j = 0; j < this._n; ++j) {
                System.out.print(this._f_matrix[j + i * this._n] + ", ");
            } System.out.println();
        } System.out.println("-----");
    }

    public void print_range(int from_i, int to_i, int from_j, int to_j){
        System.out.println("-----");
        for(int i = from_i; i < to_i; ++i) {
            for (int j = from_j; j < to_j; ++j) {
                System.out.print(this._f_matrix[j + i * this._n] + " ");
            } System.out.println();
        } System.out.println("-----");
    }

    public void print_lines(int from_i, int to_i){
        System.out.println("-----");
        for(int i = from_i; i < to_i; ++i) {
            for (int j = 0; j < this._n; ++j) {
                System.out.print(this._f_matrix[j + i * this._n] + " ");
            } System.out.println();
        } System.out.println("-----");
    }
}
