package com.example.unipiaudiostories.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.unipiaudiostories.data.Story
import java.util.Locale

/**
 * Handles voice interaction logic including Speech-to-Text (STT) and Text-to-Speech (TTS).
 * Manages the conversation flow state machine.
 */
class VoiceAssistant(
    private val context: Context,
    private val onStatusChange: (String, Boolean) -> Unit,
    private val onNavigateToStats: () -> Unit,
    private val onNavigateToStory: (String) -> Unit
) {
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var stories: List<Story> = emptyList()

    // Holds the pending navigation action to execute after speech finishes
    private var pendingNavigation: (() -> Unit)? = null

    private enum class State { IDLE, ASKING_NAVIGATION, ASKING_STORY }
    private var currentState = State.IDLE

    init {
        // Initialize Text-to-Speech engine
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH // Enforce English for voice commands

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        updateUI("Speaking...", false)
                    }

                    override fun onDone(utteranceId: String?) {
                        // Ensure UI updates and logic run on the main thread
                        Handler(Looper.getMainLooper()).post {
                            if (utteranceId == "NAV_EXIT") {
                                // Execute pending navigation if this was an exit message
                                pendingNavigation?.invoke()
                                pendingNavigation = null
                            } else {
                                // Otherwise, start listening for user input
                                startListening()
                            }
                        }
                    }

                    override fun onError(utteranceId: String?) {}
                })
            }
        }

        // Initialize Speech Recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                updateUI("Listening...", true)
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                updateUI("Processing...", false)
            }
            override fun onError(error: Int) {
                updateUI("Didn't catch that. Tap Retry.", false)
                currentState = State.IDLE
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    processCommand(matches[0].lowercase())
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun updateUI(msg: String, listening: Boolean) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onStatusChange(msg, listening)
        } else {
            Handler(Looper.getMainLooper()).post { onStatusChange(msg, listening) }
        }
    }

    /**
     * Starts the voice command session.
     */
    fun startSession(currentStories: List<Story>) {
        this.stories = currentStories
        currentState = State.ASKING_NAVIGATION
        speak("Where do you want to go? Statistics or Home?", "voice_cmd")
    }

    /**
     * Retries the last action or restarts listening.
     */
    fun retry() {
        if (currentState == State.IDLE) startSession(stories)
        else startListening()
    }

    private fun speak(text: String, utteranceId: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)
        }
        speechRecognizer?.startListening(intent)
    }

    private fun processCommand(command: String) {
        when (currentState) {
            State.ASKING_NAVIGATION -> {
                if (command.contains("statistics") || command.contains("stats")) {
                    pendingNavigation = { onNavigateToStats() }
                    speak("Navigating to statistics.", "NAV_EXIT")
                    currentState = State.IDLE

                } else if (command.contains("home")) {
                    currentState = State.ASKING_STORY
                    val sb = StringBuilder("Here are the stories. ")
                    stories.forEachIndexed { index, story ->
                        sb.append("Say ${index + 1} for ${story.title}. ")
                    }
                    speak(sb.toString(), "voice_cmd")

                } else {
                    speak("Please say Statistics or Home.", "voice_cmd")
                }
            }
            State.ASKING_STORY -> {
                val index = parseNumber(command)
                if (index != -1 && index < stories.size) {
                    val selectedStory = stories[index]
                    pendingNavigation = { onNavigateToStory(selectedStory.id) }
                    speak("Opening ${selectedStory.title}", "NAV_EXIT")
                    currentState = State.IDLE

                } else {
                    speak("Story not found. Please say a number.", "voice_cmd")
                }
            }
            else -> {}
        }
    }

    /**
     * Parses spoken numbers (e.g., "one", "1") into integer indices.
     */
    private fun parseNumber(text: String): Int {
        if (text.contains("1") || text.contains("one")) return 0
        if (text.contains("2") || text.contains("two")) return 1
        if (text.contains("3") || text.contains("three")) return 2
        if (text.contains("4") || text.contains("four")) return 3
        if (text.contains("5") || text.contains("five")) return 4
        return -1
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}