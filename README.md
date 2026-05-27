# HolyAI <=> :)

> **⚠️ License & Commercial Use**
> This project is licensed under the CC BY-NC 4.0 License. It is completely free for personal, educational, and non-commercial use, provided that proper credit is given to **OpEx-Experiment001 (Aadarsh Dimri)**.
> 
> **You are strictly prohibited from using this software, or modified versions of it, for commercial purposes or inside a business.**
> 
> If you wish to use this software in a commercial product, integrate it into a business, or generate revenue from it, you must purchase a Commercial License. Please contact `aadarshdimri0218@gmail.com` to discuss licensing fees and revenue-sharing agreements.

A zero-dependency Java CLI agent featuring Conversational Memory, a native RAG engine, and a Reinforcement Learning feedback loop. It transforms any local (Ollama) or cloud (OpenAI) LLM into an interactive, self-learning coding assistant.

## Overview
Unlike standard API wrappers that simply forward prompts, **HolyAI** implements complex AI behaviors natively in pure Java without relying on heavy frameworks like Spring Boot or Python libraries like LangChain. It serves as an excellent Project-Based Learning (PBL) sandbox to understand how advanced AI architectural patterns can be built from scratch.

## Core Features

*   **🧠 Conversational Memory (Stateful Chat):** The application maintains session history using a custom `ChatSession` manager, actively serializing past interactions into the JSON payload so the AI remembers context across multiple turns.
*   **📚 Native RAG Engine (Retrieval-Augmented Generation):** Features an in-memory document loader and Jaccard-similarity retriever that scans local `.txt` or `.md` files inside the `docs/` folder, injecting highly relevant context directly into the AI's system prompt before inference.
*   **🔄 Agentic Feedback Loop (RL Simulation):** Simulates Reinforcement Learning from Human Feedback (RLHF). Users can rate AI responses (`+` or `-`), saving positive interactions to a local CSV database. The system automatically retrieves these past successes and uses them as Few-Shot Prompting examples to align future answers with user preferences.
*   **🌐 Universal Provider Support:** A dynamic HTTP client that communicates via the standard OpenAI Chat format, seamlessly supporting cloud providers (OpenAI) as well as offline, privacy-first local models (Ollama, LM Studio). Includes auto-recovery logic to launch offline local models dynamically via terminal commands if a connection is refused.

## Setup Instructions

### 1. Configuration
Create a file named `config.properties` in the root directory:
```properties
# The API endpoint for the AI Provider (OpenAI format compatible)
ai.url=http://localhost:11434/api/chat

# The exact name of the model you wish to run
ai.model=phi3

# Your API Key (Leave blank if running local models like Ollama)
ai.api_key=

# Auto-Recovery: Terminal command to execute if the AI server is offline.
ai.startup_command=ollama run phi3
```

### 2. Compilation
Because HolyAI has zero external dependencies, you can compile it directly with `javac`:
```bash
javac Main.java client/*.java memory/*.java rag/*.java rl/*.java service/*.java
```

### 3. Usage
Run the main class to start the interactive REPL:
```bash
java Main
```
*   Type your prompt.
*   Type `+` or `-` after a response to train the AI.
*   Type `exit` to close the session.

## Custom Knowledge (RAG)
To teach the AI custom information, simply place any `.txt` or `.md` files inside the `docs/` directory. The AI will automatically read them and reference them when answering relevant questions.
