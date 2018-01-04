package net.tofvesson.reflection;

public class NotAutoBoxedException extends Exception {
    public NotAutoBoxedException(){ super(); }
    public NotAutoBoxedException(Throwable t){ super(t); }
    public NotAutoBoxedException(String reason){ super(reason); }
}
