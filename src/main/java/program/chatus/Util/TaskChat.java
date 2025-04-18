package program.chatus.Util;

import javafx.concurrent.Task;
import org.springframework.ai.chat.client.ChatClient;

public class TaskChat extends Task<String> {
    protected String chatext;
    public TaskChat(String message){
        this.chatext=message;
    }
    @Override
    protected String call() throws Exception {
        System.out.println("TaskChat started with message: " + chatext);

        var chatClient = ContextUtil.getContext().getBean(ChatClient.class);
        var llmResoponse = chatClient.prompt().user(chatext).call().content();

        System.out.println("Received response: " + llmResoponse);

        updateValue(llmResoponse);  // This should trigger the listener in the UI thread

        return llmResoponse;
    }

}
