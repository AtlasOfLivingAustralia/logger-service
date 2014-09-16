package org.ala.jpa.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Represents a remote address. Used for write access control.
 */
@Entity
@Table(name="remote_address")
public class RemoteAddress extends PersistentEntity implements Serializable {

    private static final long serialVersionUID = -1126253054708678533L;

    @Id
    @Column(name="ip")
    private String ip;

    @Column(name="host_name")
    private String hostName;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
