package net.opentsdb.core;

public class Prefix {
    private String systemId = TSDB.DEFAULT_SYSTEM;
    private Long orgId = TSDB.DEFAULT_ORG;

    public Prefix(Long orgId, String systemId){
        this.orgId = orgId;
        this.systemId = systemId;
    }
    public Prefix(){

    }

    @Override
    public String toString() {
        return String.format("%d.%s", this.orgId, this.systemId);
    }
}
