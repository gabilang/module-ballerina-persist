/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.persist.compiler.codeaction;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.plugins.codeaction.CodeAction;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionInfo;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_001;
import static io.ballerina.stdlib.persist.compiler.Utils.getNumericDiagnosticProperty;
import static io.ballerina.stdlib.persist.compiler.Utils.getTextRangeArgument;
import static io.ballerina.stdlib.persist.compiler.codeaction.PersistCodeActionName.MARK_FIELD_AS_IDENTITY_FIELD;

/**
 * Code action to mark a field as identity.
 */
public class MarkFieldAsIdentityField implements CodeAction {

    public static final String READONLY_ADD_TEXT_RANGE = "readonly.add.text.range";

    @Override
    public List<String> supportedDiagnosticCodes() {
        return List.of(PERSIST_001.getCode());
    }

    @Override
    public Optional<CodeActionInfo> codeActionInfo(CodeActionContext codeActionContext) {
        List<DiagnosticProperty<?>> properties = codeActionContext.diagnostic().properties();

        TextRange lineAddLocation = TextRange.from(getNumericDiagnosticProperty(properties, 0), 0);

        CodeActionArgument lineAddLocationArg = CodeActionArgument.from(READONLY_ADD_TEXT_RANGE, lineAddLocation);

        return Optional.of(CodeActionInfo.from(MessageFormat.format("Mark field ''{0}'' as identity field",
                codeActionContext.diagnostic().diagnosticInfo().messageFormat()), List.of(lineAddLocationArg)));
    }

    @Override
    public List<DocumentEdit> execute(CodeActionExecutionContext context) {
        TextRange startFrom = getTextRangeArgument(context, READONLY_ADD_TEXT_RANGE);
        if (startFrom == null) {
            return Collections.emptyList();
        }

        SyntaxTree syntaxTree = context.currentDocument().syntaxTree();

        List<TextEdit> textEdits = new ArrayList<>();
        textEdits.add(TextEdit.from(startFrom, "readonly "));

        TextDocumentChange change = TextDocumentChange.from(textEdits.toArray(new TextEdit[0]));
        TextDocument modifiedTextDocument = syntaxTree.textDocument().apply(change);
        return Collections.singletonList(new DocumentEdit(context.fileUri(), SyntaxTree.from(modifiedTextDocument)));
    }

    @Override
    public String name() {
        return MARK_FIELD_AS_IDENTITY_FIELD.getName();
    }
}