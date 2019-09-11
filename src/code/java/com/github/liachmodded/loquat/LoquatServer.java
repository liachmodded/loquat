package com.github.liachmodded.loquat;

public interface LoquatServer {

    LoquatConvention getConvention();

    LoquatConvention createConvention(Loquat mod);

    void clearConvention();

}
