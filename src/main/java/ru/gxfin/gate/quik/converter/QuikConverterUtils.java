package ru.gxfin.gate.quik.converter;

import org.jetbrains.annotations.Nullable;
import org.springframework.util.StringUtils;
import ru.gx.fin.common.dris.out.DealDirection;
import ru.gx.fin.gate.quik.provider.out.QuikDealDirection;
import ru.gx.fin.gate.quik.provider.out.QuikSecurity;

public abstract class QuikConverterUtils {
    @Nullable
    public static String getCurrencyCodeByQuikSecurity(@Nullable final QuikSecurity quikInstrument) {
        if (quikInstrument == null) {
            return null;
        }

        var result = quikInstrument.getCurrencyId();
        if (!StringUtils.hasLength(result) || "SUR".equals(result)) {
            result = "RUB";
        };

        return result;
    }

    @Nullable
    public static DealDirection getDealDirection(@Nullable final QuikDealDirection quikDealDirection) {
        if (quikDealDirection == null) {
            return null;
        }
        if (quikDealDirection == QuikDealDirection.B) {
            return DealDirection.B;
        }
        return DealDirection.S;
    }
}
