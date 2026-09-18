package org.tafel.squating.domain.value;
import java.util.List;

public record  MailSnapshot(
    List<String> mxRecords,
    boolean mxConfigured
) {
    public  MailSnapshot{
        mxRecords = mxRecords != null ? List.copyOf(mxRecords) : List.of();
    }
}