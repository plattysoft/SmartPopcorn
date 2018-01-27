package com.plattysoft.smartpopcorn;

/**
 * Created by Raul Portales on 27/01/18.
 */

interface CommandListener {
    void onCommandReceived(PopcornCommand enumCommand);
}
