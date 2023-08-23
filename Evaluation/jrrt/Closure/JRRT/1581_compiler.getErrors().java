package com.google.javascript.jscomp;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class JsMessageExtractor  {
  final private JsMessage.Style style;
  final private JsMessage.IdGenerator idGenerator;
  final private CompilerOptions options;
  public JsMessageExtractor(JsMessage.IdGenerator idGenerator, JsMessage.Style style) {
    this(idGenerator, style, new CompilerOptions());
  }
  public JsMessageExtractor(JsMessage.IdGenerator idGenerator, JsMessage.Style style, CompilerOptions options) {
    super();
    this.idGenerator = idGenerator;
    this.style = style;
    this.options = options;
  }
  public Collection<JsMessage> extractMessages(SourceFile ... inputs) throws IOException {
    return extractMessages(ImmutableList.copyOf(inputs));
  }
  public  <T extends com.google.javascript.jscomp.SourceFile> Collection<JsMessage> extractMessages(Iterable<T> inputs) throws IOException {
    Compiler compiler = new Compiler();
    compiler.init(ImmutableList.<SourceFile>of(), Lists.newArrayList(inputs), options);
    compiler.parseInputs();
    ExtractMessagesVisitor extractCompilerPass = new ExtractMessagesVisitor(compiler);
    JSError[] var_1581 = compiler.getErrors();
    if(var_1581.length == 0) {
      extractCompilerPass.process(null, compiler.getRoot());
    }
    JSError[] errors = compiler.getErrors();
    if(errors.length > 0) {
      StringBuilder msg = new StringBuilder("JSCompiler errors\n");
      MessageFormatter formatter = new LightweightMessageFormatter(compiler);
      for (JSError e : errors) {
        msg.append(formatter.formatError(e));
      }
      throw new RuntimeException(msg.toString());
    }
    return extractCompilerPass.getMessages();
  }
  
  private class ExtractMessagesVisitor extends JsMessageVisitor  {
    final private List<JsMessage> messages = Lists.newLinkedList();
    private ExtractMessagesVisitor(AbstractCompiler compiler) {
      super(compiler, true, style, idGenerator);
    }
    public Collection<JsMessage> getMessages() {
      return messages;
    }
    @Override() void processJsMessage(JsMessage message, JsMessageDefinition definition) {
      if(!message.isExternal()) {
        messages.add(message);
      }
    }
  }
}