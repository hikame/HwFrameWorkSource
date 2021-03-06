package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.SecurityVerify;
import gov.nist.javax.sip.header.ims.SecurityVerifyList;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;

public class SecurityVerifyParser extends SecurityAgreeParser {
    public SecurityVerifyParser(String security) {
        super(security);
    }

    protected SecurityVerifyParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        dbg_enter("SecuriryVerify parse");
        SecurityVerifyList secVerifyList;
        try {
            headerName(TokenTypes.SECURITY_VERIFY);
            secVerifyList = (SecurityVerifyList) super.parse(new SecurityVerify());
            return secVerifyList;
        } finally {
            secVerifyList = "SecuriryVerify parse";
            dbg_leave(secVerifyList);
        }
    }
}
