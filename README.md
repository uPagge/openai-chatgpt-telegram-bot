# Personal ChatGPT Telegram Bot

The project uses your ChatGPT token to access the ChatGPT API and let you chat with ChatGPT directly in Telegram.  
[See documentation for details](https://docs.struchkov.dev/chatgpt-telegram-bot/en/latest/)

Enough words, let's launch your personal ChatGPT Telegram bot. 🚀

Key Features:

* Saving the context of a conversation
* Access can be restricted by specifying a list of allowed users.
* Support for multiple chats for one user
* Support markdown in answers
* Ability to set behavior for the conversation, which will be preserved even when the context is cleared
* Docker and Proxy support
* Possibility to check expenses for the current month
* Tells you how many tokens you spent to generate a response
* Support Telegram Inline Mode

## Preparing

* You must have [Docker installed](https://docs.docker.com/engine/install/). You can run the project both on the PC and on the server.
* You must [register a bot in Telegram](https://t.me/BotFather) and get an access token.
* You also need [ChatGPT access token](https://platform.openai.com/account/api-keys).
* You must know your telegramId. [You can find it out here.](https://t.me/myidbot).

## Environment variables

* `TELEGRAM_BOT_TOKEN` - The bot access token you got from GodFather. Example: 1234567890:XXX_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
* `TELEGRAM_BOT_USERNAME` - Specify a name with the ending bot here, not a public name. Example: Undusted_bot
* `TELEGRAM_PERSON_ID` - The IDs of the users on Telegram who are allowed access. Example: 1234567, 56789045
* `ADMIN_TELEGRAM_PERSON_ID` - The IDs of the users on Telegram who are allowed admin access. Example: 1234567
* `CHAT_GPT_TOKEN` - OpenAI API access token.

## Docker Run

The following platforms are supported: linux/amd64,linux/arm64/v8

``` bash
docker run -it --name chatgpt-telegram-bot \
    --env TELEGRAM_BOT_TOKEN= \
    --env TELEGRAM_BOT_USERNAME= \
    --env TELEGRAM_PERSON_ID= \
    --env ADMIN_TELEGRAM_PERSON_ID= \
    --env CHAT_GPT_TOKEN= \
    upagge/chatgpt-telegram-bot:develop
```

### Telegram Proxy
If you have Telegram blocked, you can specify proxy settings to connect.

``` bash   
docker run -it --name chatgpt-telegram-bot \
    --env TELEGRAM_BOT_TOKEN= \
    --env TELEGRAM_BOT_USERNAME= \
    --env TELEGRAM_PERSON_ID= \
    --env ADMIN_TELEGRAM_PERSON_ID= \
    --env CHAT_GPT_TOKEN= \
    --env TELEGRAM_PROXY_ENABLE=true \
    --env TELEGRAM_PROXY_HOST= \
    --env TELEGRAM_PROXY_PORT= \
    --env TELEGRAM_PROXY_TYPE=SOCKS5 \
    --env TELEGRAM_PROXY_USERNAME= \
    --env TELEGRAM_PROXY_PASSWORD= \
    upagge/chatgpt-telegram-bot:latest
```

* Available options `SOCKS5`, `SOCKS4`, `HTTP`.
* Optional. If there is no authorization, you can leave it blank.

## DockerCompose

Don't forget to create a file with the variable `.env`.

 ``` yaml
version: '3.8'
services:
  chat-gpt:
    image: upagge/chatgpt-telegram-bot:latest
    container_name: chatgpt-bot
    restart: always
    environment:
      TELEGRAM_BOT_TOKEN: ${TELEGRAM_BOT_TOKEN}
      TELEGRAM_BOT_USERNAME: ${TELEGRAM_BOT_USERNAME}
      TELEGRAM_PERSON_ID: ${TELEGRAM_PERSON_ID}
      ADMIN_TELEGRAM_PERSON_ID: ${ADMIN_TELEGRAM_PERSON_ID}
      CHAT_GPT_TOKEN: ${CHAT_GPT_TOKEN}
```

``` text
TELEGRAM_BOT_TOKEN=
TELEGRAM_BOT_USERNAME=
TELEGRAM_PERSON_ID=
ADMIN_TELEGRAM_PERSON_ID=
CHAT_GPT_TOKEN=
```