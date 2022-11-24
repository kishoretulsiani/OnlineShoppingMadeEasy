

package org.shopping.company.gateway;

import io.vertx.core.Launcher;

public class ApplicationLauncher extends Launcher {

    public static void main(String[] args) {
        (new ApplicationLauncher()).dispatch(args);
    }

}
