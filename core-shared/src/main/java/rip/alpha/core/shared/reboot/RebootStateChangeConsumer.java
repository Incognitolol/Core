package rip.alpha.core.shared.reboot;

public interface RebootStateChangeConsumer {

    void onStateChange(int currentTime);

}
