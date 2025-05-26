package dam.tfg.blinky

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

/**
 * Custom Application class for Blinky app.
 * Initializes libraries and provides application-wide functionality.
 */
class BlinkyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize ThreeTenABP library for java.time backport
        AndroidThreeTen.init(this)
    }
}