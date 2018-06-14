package hoschschulebochum.souldRing;

import hoschschulebochum.souldRing.IShouldRing;

public class ShouldRing implements IShouldRing {
    @Override
    public boolean doesLocationFit() {
        return true;
    }

    @Override
    public boolean doesPersonStartToMove() {
        return true;
    }

    @Override
    public boolean doesLigthGetOn() {
        return true;
    }
}
