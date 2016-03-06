package stydying.algo.com.algostudying.utils.vectors;

import java.io.Serializable;

/**
 * Created by Anton on 28.02.2016.
 */
public class Vector3i implements Serializable {

    public int x;
    public int y;
    public int z;

    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return "{ x=" + x + ", y=" + y + ", z=" + z + " }";
    }
}
