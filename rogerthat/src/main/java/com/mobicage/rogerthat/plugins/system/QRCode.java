package com.mobicage.rogerthat.plugins.system;

/**
 * Created by bart on 25/01/2017.
 */

public class QRCode {
    public final String name;
    public final String content;

    public QRCode(String name, String content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QRCode qrCode = (QRCode) o;

        if (name != null ? !name.equals(qrCode.name) : qrCode.name != null) return false;
        return content != null ? content.equals(qrCode.content) : qrCode.content == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
