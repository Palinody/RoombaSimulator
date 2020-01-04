package Math;

public class Function{

    public static void sigmoid(Matrix data){
        for(int i = 0; i < data.getM(); ++i){
            for(int j = 0; j < data.getN(); ++j){
                float value = data.get(i, j);
                data.set(i, j, 1F / (1F + (float)Math.exp(-value)));
            }
        }
    }

    public static void reLU(Matrix data){
        for(int i = 0; i < data.getM(); ++i){
            for(int j = 0; j < data.getN(); ++j){
                float value = data.get(i, j);
                data.set(i, j, Math.max(0, value));
            }
        }
    }

    public static void tanh(Matrix data){
        for(int i = 0; i < data.getM(); ++i){
            for(int j = 0; j < data.getN(); ++j){
                float value = data.get(i, j);
                data.set(i, j, (float)Math.tanh(value));
            }
        }
    }


    ///////////////////////////////////////////////
    //                  MISCELLANEOUS            //
    ///////////////////////////////////////////////

    public static void log(Matrix data){
        for(int i = 0; i < data.getM(); ++i){
            for(int j = 0; j < data.getN(); ++j){
                float value = data.get(i, j) + 1e-8F;
                data.set(i, j, (float)Math.log(value));
            }
        }
    }

    public static void sqrt(Matrix data){
        for(int i = 0; i < data.getM(); ++i){
            for(int j = 0; j < data.getN(); ++j){
                float value = data.get(i, j);
                data.set(i, j, (float)Math.sqrt(value));
            }
        }
    }

    public static void pow(Matrix data, int coeff){
        for(int i = 0; i < data.getM(); ++i){
            for(int j = 0; j < data.getN(); ++j){
                float value = data.get(i, j);
                data.set(i, j, (float)Math.pow(value, coeff));
            }
        }
    }
}