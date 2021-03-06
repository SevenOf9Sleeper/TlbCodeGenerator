package eu.doppel_helix.jna.tlbcodegenerator.imp;

import com.sun.jna.platform.win32.COM.TypeInfoUtil;
import com.sun.jna.platform.win32.COM.TypeInfoUtil.TypeInfoDoc;
import com.sun.jna.platform.win32.COM.TypeLibUtil;
import com.sun.jna.platform.win32.OaIdl;
import com.sun.jna.platform.win32.OaIdl.INVOKEKIND;
import java.util.LinkedList;
import java.util.List;

public class TlbFunctionCall {
    private final static int FUNCFLAG_FSOURCE = 0x2;
    
    private final String methodName;
    private final String documentation;
    private final String returnType;
    private final boolean source;
    private final boolean property;
    private final boolean setter;
    private final Short vtableId;
    private final OaIdl.MEMBERID memberId;
    private final int typeIndex;
    private final List<TlbFunctionCallParam> params = new LinkedList<>();

    public TlbFunctionCall(TypeLib tl, int index, OaIdl.FUNCDESC funcDesc, TypeInfoUtil typeInfoUtil) {
        TypeInfoDoc typeInfoDoc = typeInfoUtil.getDocumentation(funcDesc.memid);
        methodName = typeInfoDoc.getName();
        documentation = typeInfoDoc.getDocString();
        if (funcDesc.invkind.value == INVOKEKIND.INVOKE_FUNC.value) {
            property = false;
            setter = false;
        } else if (funcDesc.invkind.value == INVOKEKIND.INVOKE_PROPERTYGET.value) {
             property = true;
             setter = false;
        } else if (funcDesc.invkind.value == INVOKEKIND.INVOKE_PROPERTYPUT.value 
                || funcDesc.invkind.value == INVOKEKIND.INVOKE_PROPERTYPUTREF.value) {
            property = true;
            setter = true;
        } else  {
            throw new RuntimeException(String.format("Failed to parse function '%s': %d",
                    methodName,
                    funcDesc.invkind
                    ));
        }

        source = (funcDesc.wFuncFlags.intValue() & FUNCFLAG_FSOURCE) > 0;
        typeIndex = index;
        returnType = tl.getType(typeInfoUtil, funcDesc);
        vtableId = funcDesc.oVft.shortValue();
        memberId = funcDesc.memid;
        
        short paramCount = funcDesc.cParams.shortValue();
        String[] names = typeInfoUtil.getNames(funcDesc.memid, paramCount + 1);
        for (int i = 0; i < paramCount; i++) {
            String paramName;
            if(i < (names.length - 1)) {
                paramName = names[i + 1];
            } else {
                paramName = "param" + i;
            }
            params.add(new TlbFunctionCallParam(tl, typeInfoUtil, funcDesc, i, paramName));
        }
    }
    
    public String getMethodName() {
        return methodName;
    }

    public String getDocumentation() {
        return documentation;
    }

    public String getReturnType() {
        return returnType;
    }

    public boolean isProperty() {
        return property;
    }

    public boolean isSetter() {
        return setter;
    }

    public short getVtableId() {
        return vtableId;
    }

    public OaIdl.MEMBERID getMemberId() {
        return memberId;
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public List<TlbFunctionCallParam> getParams() {
        return params;
    }

    public boolean isSource() {
        return source;
    }

    @Override
    public String toString() {
        return "TlbFunctionCall{" + "methodName=" + methodName + ", returnType=" +
                returnType + ", property=" + property + ", params=" + params +
                '}';
    }

    
}
