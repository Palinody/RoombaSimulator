package Utils;
import java.util.concurrent.ThreadLocalRandom;

public class PRNG {

    public float gaussian(float mean, float sd){
        ThreadLocalRandom generator = ThreadLocalRandom.current();
        return mean + (float)generator.nextGaussian() * sd;
    }

    public float xavier(float m, float n){
        float xavier_sd = (float)Math.sqrt(2/(m+n));
        ThreadLocalRandom generator = ThreadLocalRandom.current();
        return (float)generator.nextGaussian() * xavier_sd;
    }

    public float uniform(float lower, float upper){
        ThreadLocalRandom generator = ThreadLocalRandom.current();
        return lower + generator.nextFloat() * (upper - lower);
    }

    public int randInt(int lower, int upper){
        ThreadLocalRandom generator = ThreadLocalRandom.current();
        return lower + generator.nextInt(lower, upper+1);
    }

}
