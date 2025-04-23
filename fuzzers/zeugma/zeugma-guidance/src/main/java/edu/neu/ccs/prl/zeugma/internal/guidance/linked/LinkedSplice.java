package edu.neu.ccs.prl.zeugma.internal.guidance.linked;

import edu.neu.ccs.prl.zeugma.internal.guidance.modify.ModifierUtil;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.Interval;

public final class LinkedSplice {
    private final MethodCallVertex applicationPoint;
    private final ByteList replacements;

    public LinkedSplice(MethodCallVertex applicationPoint, MethodCallVertex donationPoint, ByteList donor) {
        this.applicationPoint = applicationPoint;
        Interval interval = donationPoint.getSourceInterval();
        this.replacements = donor.subList(interval.getStart(), interval.getEnd());
    }

    public ByteList apply(ByteList parent) {
        return ModifierUtil.replaceRange(parent,
                applicationPoint.getSourceInterval().getStart(),
                applicationPoint.getSourceInterval().size(),
                replacements);
    }

    public int getStart() {
        return applicationPoint.getSourceInterval().getStart();
    }

    public int getEnd() {
        return applicationPoint.getSourceInterval().getEnd();
    }
}
