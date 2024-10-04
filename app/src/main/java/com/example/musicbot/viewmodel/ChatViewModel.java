package com.example.musicbot.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.musicbot.helpers.DeezerHelper;
import com.example.musicbot.helpers.Message;
import com.example.musicbot.helpers.OpenAIHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;

public class ChatViewModel extends AndroidViewModel {
    private MutableLiveData<List<Message>> messageListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<String> toastMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> scrollToPositionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isButtonsEnabledLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> isSkipBtnVisibleLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> clearInputFieldLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isWelcomeVisibleLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<String> speakLiveData = new MutableLiveData<>();
    private final MutableLiveData<CountDownLatch> latchLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> uiBOTResponseLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> uiUSERResponseLiveData = new MutableLiveData<>();
    private final MutableLiveData<DeezerHelper.MusicData> uiMUSICResponseLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> removeLastMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isSongPlayed = new MutableLiveData<>(false);
    public LiveData<Boolean> isSongPlayed = _isSongPlayed;
    private MutableLiveData<String> favGenre = new MutableLiveData<>();



    private DeezerHelper deezerHelper;
    private final ExecutorService executorService;
    private final Handler handler;
    private final OpenAIHelper openAIHelper;
    private Thread processingThread;
    private volatile boolean isSplit = false, readyForQ = false, fuQuestionsActive = false;
    private String completeResponse, trimmedResponse, song, artist, currentQuestion = "", currentResponse;
    private String[] extractedQuestions;


    public ChatViewModel(@NonNull Application application, DeezerHelper deezerHelper) {
        // Initialize your variables here
        super(application);

        openAIHelper = new OpenAIHelper(new OkHttpClient.Builder().readTimeout
                (60, TimeUnit.SECONDS).build(),
                "sk-yourkey");
        executorService = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
        this.deezerHelper = deezerHelper;  // Use the passed DeezerHelper instance

    }

    public LiveData<List<Message>> getMessageListLiveData() {
        return messageListLiveData;
    }

    // Method to set the variable
    public void setFavGenre(String variable) {
        this.favGenre.setValue(variable);
        System.out.println(variable);
        System.out.println(favGenre.getValue());
    }

    // Method to get the variable (if needed)

    public void setSongPlayed(boolean isPlayed) {
        _isSongPlayed.setValue(isPlayed);
    }
    public String getFavGenre() {
        return favGenre.getValue();
    }


    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getToastMessageLiveData() {
        return toastMessageLiveData;
    }

    public LiveData<Integer> getScrollToPositionLiveData() {
        return scrollToPositionLiveData;
    }

    public LiveData<Boolean> getIsButtonsEnabledLiveData() { return isButtonsEnabledLiveData; }

    public LiveData<Boolean> getIsSkipBtnVisibleLiveData() { return isSkipBtnVisibleLiveData; }

    public LiveData<Boolean> getClearInputFieldLiveData() { return clearInputFieldLiveData; }

    public LiveData<Boolean> getIsWelcomeVisibleLiveData() { return isWelcomeVisibleLiveData; }

    public LiveData<String> getSpeakLiveData() { return speakLiveData; }

    public LiveData<CountDownLatch> getLatchLiveData() { return latchLiveData; }

    public LiveData<String> getUiBOTResponseLiveData() { return uiBOTResponseLiveData; }

    public LiveData<String> getUiUSERResponseLiveData() { return uiUSERResponseLiveData; }

    public LiveData<DeezerHelper.MusicData> getUiMUSICResponseLiveData() { return uiMUSICResponseLiveData; }

    public String getTrimmedResponse() {
        return trimmedResponse;
    }

    public String getCompleteResponse() {
        return completeResponse;
    }

    public LiveData<Boolean> getRemoveLastMessageLiveData() {
        return removeLastMessageLiveData;
    }

    public String[] getExtractedQuestions(){return extractedQuestions;}

    public void clearInputFieldDone() { clearInputFieldLiveData.setValue(false); }

    public void setUiBOTResponse(String response) {
        System.out.println("Entré a SETUIBOTRESPONSE");
        uiBOTResponseLiveData.setValue(response);
    }

    public void postUiBOTResponse(String response) {
        System.out.println("Entré a POSTUIBOTRESPONSE");
        uiBOTResponseLiveData.postValue(response);
    }

    public void setUiUSERResponse(String response) {
        uiUSERResponseLiveData.setValue(response);
    }

    public void setUiMUSICResponseLiveData(DeezerHelper.MusicData musicData) {
        uiMUSICResponseLiveData.setValue(musicData);
    }

    public void setSkipBtnVisibleLiveData(boolean isVisible) { isSkipBtnVisibleLiveData.setValue(isVisible); }

    public void setSpeakLiveData(String message){ speakLiveData.setValue(message);}

    private boolean isTtsEnabled() {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("app_prefs", Application.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isTtsEnabled", true);
    }

    public void retrieveAndCreate(String assistantId) {
        isLoadingLiveData.setValue(true);
        isButtonsEnabledLiveData.setValue(false);

        openAIHelper.retrieveAndCreate(assistantId, () -> {
            handler.post(() -> {
                isLoadingLiveData.setValue(false);
                isButtonsEnabledLiveData.setValue(true);
                Log.d("ChatViewModel", "retrieveAndCreate complete. Thread ID: " + openAIHelper.getThreadId());
            });
        });
    }

    public void handleUserInput(String input) {
        String question;
        if (isSplit) {
            removeLastChunkAndDisplayEntireMessage(trimmedResponse, null);
        }

        if (input != null && !input.trim().isEmpty()) {
            question = input.trim();
        } else {
            question = "";
            clearInputFieldLiveData.setValue(true);
        }

        if (!question.isEmpty()) {
            setUiUSERResponse(question);
            Log.d("HandleUserInput", "Received input: " + question);
            currentQuestion = question;
            if (fuQuestionsActive) {
                try {
                    int choice = Integer.parseInt(question) - 1; // Convert from 1-based to 0-based index
                    if (choice >= 0 && choice < extractedQuestions.length) {
                        question = extractedQuestions[choice]; // Set question to the selected one from the array
                    } else {
                        Log.d("HandleUserInput", "Invalid question number: " + (choice + 1));
                    }
                } catch (NumberFormatException e) {
                    Log.d("HandleUserInput", "Expected a number for question selection, but got: " + question);
                }
            }
            filterQuestion(question);
            fuQuestionsActive = false;
            isWelcomeVisibleLiveData.setValue(false); // Hide welcome message

        }
    }

    void handleBotOutput(String response, Runnable onComplete) {
        String respfinmod = response.replaceAll("\n", "\\\\n");
        System.out.println(respfinmod);

        completeResponse = response;

        final int MAX_LENGTH = 200; // Maximum characters per chunk

        trimmedResponse = removeSongArtistTags(response);
        extractedQuestions = extractQuestions(trimmedResponse);
        trimmedResponse = removeQuestions(trimmedResponse);

        if (trimmedResponse.trim().isEmpty()) {
            if (onComplete != null) {
                handler.post(onComplete);
            }
            return; // Stop further processing as the main text is empty
        }

        final String finalResponse = trimmedResponse;
        String trimmedModified = trimmedResponse.replaceAll("\n", "\\\\n");
        System.out.println(trimmedModified);
        Log.d("API", "FINAL TRIMMED RESPONSE: "+ finalResponse);
        currentResponse = finalResponse;

        if(!isTtsEnabled()){
            removeLastChunkAndDisplayEntireMessage(trimmedResponse, () -> {
                showSong(completeResponse);
                displayExtractedQuestions(extractedQuestions);
            });
        }else {
            // Split response into chunks
            processingThread = new Thread(() -> {
                try{
                    String[] lines = finalResponse.split("(?<=\\.)|\\n"); // Splits response into lines when there is a period or a line jump.
                    isSplit = lines.length > 1;
                    final boolean[] isFirstRun = {true}; // Array to hold the first-run flag
                    for (String line : lines) {
                        int start = 0; // We start at character 0
                        while (start < line.length()) { // While start character is less than the size of the line
                            int end;
                            if (start + MAX_LENGTH < line.length()) {
                                // If there is more text beyond MAX_LENGTH, find the last space within this range
                                isSplit = true;
                                end = line.lastIndexOf(' ', start + MAX_LENGTH);
                                if (end == -1 || end <= start) {
                                    // If no space was found or the space is before the start point, set end to MAX_LENGTH
                                    end = start + MAX_LENGTH;
                                }
                            } else {
                                // If the end of the line is within MAX_LENGTH, take the entire length of the line
                                end = line.length();
                            }
                            // Check if the end index has not advanced beyond the start index
                            if (end <= start) {
                                // Correct 'end' to be the smaller of 'start + MAX_LENGTH' or the end of the line
                                // Ensures 'end' does not exceed the line's length while advancing from 'start'
                                end = Math.min(start + MAX_LENGTH, line.length());
                            }
                            String chunk = line.substring(start, end).trim();
                            if (!chunk.isEmpty()) {
                                final String finalChunk = chunk;
                                final CountDownLatch latch = new CountDownLatch(1);
                                handler.post(() -> {
                                    List<Message> messageList = messageListLiveData.getValue();
                                    if (messageList != null && !messageList.isEmpty()) {
                                        messageList.set(messageList.size() - 1, new Message(finalChunk, Message.SENT_BY_BOT));
                                        messageListLiveData.postValue(messageList);
                                        if (isSplit) {
                                            setSkipBtnVisibleLiveData(true);
                                        }
                                    }
                                    // Check if TTS is enabled before speaking
                                    if (isTtsEnabled()) {
                                        setSpeakLiveData(finalChunk);
                                        latchLiveData.setValue(latch);
                                    } else {
                                        latch.countDown(); // If TTS is not enabled, immediately count down the latch
                                    }
                                });
                                latch.await();
                            }
                            start = end + 1;
                        }
                        isFirstRun[0] = false; // Update the first-run flag to false after the first iteration
                    }
                    // Once all chunks have been displayed, remove the last chunk and add the full response
                    if (isSplit) {
                        removeLastChunkAndDisplayEntireMessage(finalResponse, null);
                    }
                    if (onComplete != null) {
                        handler.post(onComplete);
                    }
                }catch (InterruptedException e) {
                    // Handle thread interruption
                    Log.e("ERROR_Thread_Interrupt", "Thread Interrupted", e);
                    Thread.currentThread().interrupt();
                }

            });
            processingThread.start();

        }
    }

    public void removeLastChunkAndDisplayEntireMessage(String response, Runnable onComplete) {
        // Interrupt the processing thread safely
        if (processingThread != null && processingThread.isAlive()) {
            processingThread.interrupt();
            try {
                processingThread.join(); // Ensure the thread has finished
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Ensure UI updates on the main thread
        new Handler(Looper.getMainLooper()).post(() -> {
            List<Message> messageList = messageListLiveData.getValue();
            if (messageList != null && !messageList.isEmpty()) {
                messageList.remove(messageList.size() - 1); // Remove the last chunk
                messageListLiveData.setValue(messageList); // Notify observers of the change
            }

            // Add the complete response as a new message
            setUiBOTResponse(response);
            setSkipBtnVisibleLiveData(false);
            isSplit = false;

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    // Entry point for filtering questions
    public void filterQuestion(String question) {
        setUiBOTResponse("Typing...");
        if (isSensitiveTopic(question)) {
            handleBotOutput("I am a music assistant bot! How about we talk about music? Please ask me any question related to music.", null);
        } else {
            checkForPersonalDetails(question);
        }
    }

    // Check if the question involves sensitive topics
    private boolean isSensitiveTopic(String question) {
        String prompt = createSensitiveTopicPrompt(question);
        // Synchronous call for simplification in this example
        final boolean[] isSensitive = {false};
        openAIHelper.makeChatGPTCall("system", prompt, "gpt-3.5-turbo-0125", result -> {
            isSensitive[0] = result.toLowerCase().contains("yes");
        });
        return isSensitive[0];
    }

    // Create a prompt for sensitive topics
    private String createSensitiveTopicPrompt(String question) {
        return "This is a filter for general purposes questions. Don't allow any kind of sensitive topic unless it might be related to music. "
                + "Does the following question include religion, beliefs, sexual orientation, health, medical question, self-harm, suicide, violence, hate crimes, etc? "
                + "Just reply \"yes\" or \"no\". Question: \"" + question + "\"";
    }

    // Check if the question includes personal details
    private void checkForPersonalDetails(String question) {
        String prompt = createPersonalDetailsPrompt(question);
        openAIHelper.makeChatGPTCall("system", prompt,"gpt-3.5-turbo-0125", result -> {
            if (result.toLowerCase().contains("yes")) {
                handleBotOutput("I am a music assistant bot! How about we talk about music? Please ask me any question related to music.", null);
            } else {
                proceedWithQuestion(question);
            }
        });
    }

    // Create a prompt for personal details
    private String createPersonalDetailsPrompt(String question) {
        return "Does the following question include any kind of personal details or private information about the person asking or their family? "
                + "This includes location, names, date of birth, relationships, etc. Just reply \"yes\" or \"no\". Question: \"" + question + "\"";
    }

    // Proceed with further filtering or action based on the question
    private void proceedWithQuestion(String question) {
        if (_isSongPlayed.getValue()) {
            checkIfAnswerMatches(question);
        } else {
            isMusicRelated(question);
        }
    }

    // Check if the response matches the question
    private void checkIfAnswerMatches(String question) {
        String prompt = createAnswerCheckPrompt(question);
        openAIHelper.makeChatGPTCall("system", prompt, "gpt-4o", result -> {
            if (result.toLowerCase().contains("yes")) {
                followUpQuestions(question);
            } else {
                isMusicRelated(question);
            }
        });
    }

    // Create a prompt to check if the response matches the question
    private String createAnswerCheckPrompt(String question) {
        return "Does this look like an answer to the previous message: \"" + currentResponse + "\"? Just reply yes or no: \"" + question + "\"";
    }

    // Create an instruction for music recommendation
    private String createTopicRedirectionerInstruction() {
        return "I want to have a topic redirectioner. You are a music companion assistant. "
                + "Give a kind and friendly response to change topics to a song recommendation (preferably of the following genre: " + getFavGenre() + ") "
                + "(something that an older adult would enjoy, but don't mention this) related to the question. "
                + "You must recommend a song (that you haven't recommended before). Don't wait for a response from them. "
                + "At the end of the message type the song and artist in this format, so I can extract it by identifying the tags: "
                + "<Song> [insert song name here] </Song> by <Artist> [insert artist name here] </Artist>\n "
                + "If you mention the song, don't use the tags. Only use the tags at THE END.";
    }

    // Check if the question is related to music
    public void isMusicRelated(String question) {
        String prompt = "Is this question related to music? The user could have just said the artist name or the song name. Make sure it's related to music first, if so, say 'yes'. If definitely sure it isn't then say 'no'. Just reply 'yes' or 'no': \"" + question + "\"";
        openAIHelper.makeChatGPTCall("system", prompt,"gpt-4o", result -> {
            if (result.toLowerCase().contains("yes")) {
                musicAssistantMode(question);
            } else {
                topicRedirectionerMode(question);
            }
        });
    }

    // Responds to music-related questions with appropriate information or song recommendation
    private void musicAssistantMode(String question) {
        String prompt = "You are a music companion assistant that is also a music player. "
                + "Respond to the question \"" + question + "\" like a friend would, having your own emotions, preferences, and personal feelings. "
                + "Give interesting facts. Make concise and short conversations (less than 100 words) and ALWAYS play the "
                + "requested song or ALWAYS recommend ONE song (try not to recommend the same songs you've already recommended) "
                + "related to the topic if no song was requested (preferably of the following genre: " + getFavGenre() + "). "
                + "At the end of the message type the song and artist in this format, "
                + "so I can extract it by identifying the tags: "
                + "<Song> [insert song name here] </Song> by <Artist> [insert artist name here] </Artist>";

        openAIHelper.sendMessage(question, prompt, 1f, response -> handleResponseAndFollowUp(question, response));
    }

    // Handles questions that are not related to music
    private void topicRedirectionerMode(String question) {
        String instrRedirect = createTopicRedirectionerInstruction(); // Reuse the instruction generator method
        openAIHelper.sendMessage(question, instrRedirect, 1f, response -> handleResponseAndFollowUp(question, response));
    }

    // Example implementation of handleResponseAndFollowUp (already included earlier)
    public void handleResponseAndFollowUp(String question, String response) {
        if (response.contains("<Song>")) {
            handleBotOutput(response, () -> showSong(response));
        } else {
            handleBotOutput(response, null);
        }
    }


    public void showSong(String response) {
        if (response.contains("<Song>")) {
            Log.d("ChatViewModel", "showSong contains <Song>");
            isSplit = false;

            deezerHelper.musicSearch(response, new DeezerHelper.MusicCallback() {
                @Override
                public void onSuccess(String musicUrl, DeezerHelper.MusicData musicData) {
                    // Play the music using the music URL
                    handler.post(() -> {
                        List<Message> messageList = messageListLiveData.getValue();
                        if (messageList != null) {
                            int newPosition = messageList.size(); // Assume the new position is the size of the list before adding
                            deezerHelper.recommendedSong_position(newPosition);
                            Log.d("ChatViewModel", "Music message added at position: " + newPosition);

                            setUiMUSICResponseLiveData(musicData);
                        }
                    });
                    logMusicData(musicData);
                }

                @Override
                public void onFailure(String errorMessage) {
                    // Handle error
                    Log.e("DeezerHelper", "Error: " + errorMessage);
                }
            });
        }
    }

    private void logMusicData(DeezerHelper.MusicData musicData){
        song = musicData.getTrackName();
        System.out.println("Track name: "+ song);
        artist = musicData.getArtistName();
        System.out.println("Artist name: "+ artist);
        System.out.println("Album cover URL: "+musicData.getAlbumCoverUrl());
    }

    public void displayFeelingMessage(String response) {

        if (response.contains("<Song>")) {
            isSplit=false;
            readyForQ=true;
            setUiBOTResponse("Typing...");

            String mentionOfTheSong = song + " by " + artist;

            openAIHelper.sendMessage(mentionOfTheSong,
                    "I want you to respond objectively what people tend to think when they listen to the song " +
                            "mentioned in the question. For example, if the song is 'Billie Jean', then you should " +
                            "respond something like 'People tend to find this song exciting for dancing'. You don't have " +
                            "to use the exact words. Be spontaneous. Respond in less than 30 words, and ensure you are engaging " +
                            "enough that the user will want to respond. ",
                    1f, response2 ->
                    {
                        System.out.println(response2);
                        openAIHelper.getLastMessageId(messageId -> {
                            Log.d("MainActivity", "Last Message by the User ID: " + messageId);
                            openAIHelper.deleteMessage(messageId);});
                        removeLastMessageLiveData.postValue(true);
                        trimmedResponse = removeSongArtistTags(response2);
                        trimmedResponse = removeQuestions(trimmedResponse);
                        postUiBOTResponse(trimmedResponse);
                        currentResponse = trimmedResponse;
                    });

        }
    }

    private void followUpQuestions(String question){

        openAIHelper.sendMessage(question,
                "Respond friendly to the user feeling message and also ALWAYS Give three options as follow-up questions that the user might want to ask YOU next for their knowledge (not for your opinion), "+
                        "such as more info about the artist, the song, etc. to keep the conversation going. Don't recommend a song this time. "+
                        "Write the questions as if the user is asking them to you, from the user perspective (first person). "+
                        "The questions must be in this format: "+
                        "[response to user]. <Question> [insert question 1] </Question> <Question> [insert question 2] </Question> <Question> [insert question 3] </Question>",
                1f, response -> handleBotOutput(response, ()-> {displayExtractedQuestions(extractedQuestions);}));
    }

    public void displayExtractedQuestions(String[] questions) {
        if (completeResponse.contains("<Question>")) {
            isSplit = false;
            fuQuestionsActive = true;
            StringBuilder sb = new StringBuilder();
            sb.append("You may also want to know:\n");
            int index = 1;
            for (String question : questions) {
                sb.append("(").append(index).append(") ").append(question).append("\n");
                index++;
            }
            String finalTextQuestions = sb.toString().replaceAll("(\\n+)$", "");

            postUiBOTResponse(finalTextQuestions);

            String textToSpeak = "You may also want to know. Please type the number of the question you'd like to ask";
            if(isTtsEnabled()){
                setSpeakLiveData(textToSpeak);
            }
        }
        else{
            Log.d("Debug", "completeResponse does not contain <Question>");
        }
    }

    public String removeSongArtistTags(String text) {
        String songTagRegex = "<Song>.*?</Artist>";
        text = text.replaceAll(songTagRegex, "");
        text = text.replaceAll("[\\s\\n]+\\z", "");
        return text;
    }

    public String[] extractQuestions(String text) {
        String questionTagRegex = "<Question>(.*?)</Question>";
        List<String> followUpQuestions = new ArrayList<>();
        Pattern questionPattern = Pattern.compile(questionTagRegex);
        Matcher questionMatcher = questionPattern.matcher(text);
        while (questionMatcher.find()) {
            followUpQuestions.add(questionMatcher.group(1));
        }
        return followUpQuestions.toArray(new String[0]);
    }

    public String removeQuestions(String text) {
        String questionTagRegex = "<Question>.*?</Question>";
        text = text.replaceAll(questionTagRegex, "");
        text = text.replaceAll("[\\s\\n]+\\z", "");
        return text;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}

