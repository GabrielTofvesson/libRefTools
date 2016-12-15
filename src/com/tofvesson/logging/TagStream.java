package com.tofvesson.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class TagStream extends PrintStream {

    private final OutputStream node;
    private List<Tag> tags = new ArrayList<Tag>();

    public TagStream(PrintStream node){
        super(node);
        this.node = node;
    }

    public TagStream(OutputStream end){
        super(end);
        this.node = end;
    }

    public List<Tag> getTags(){ return tags; }
    public void addTag(Tag t){ tags.add(t); }
    public void removeTag(Tag t){ tags.remove(t); }
    public void removeTag(int index){ tags.remove(index); }

    @Override
    public void print(String s){
        if(s==null) s="null";
        for(Tag t : tags) s = t.apply(s);
        if(node instanceof PrintStream) ((PrintStream)node).print(s);
        else try { node.write(s.getBytes()); } catch (IOException e) { e.printStackTrace(); }
    }
}
