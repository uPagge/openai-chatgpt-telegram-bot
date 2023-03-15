# Personal ChatGPT Telegram Bot

The project uses your ChatGPT token to access the ChatGPT API and let you chat with ChatGPT directly in Telegram.  
[See documentation for details](https://docs.struchkov.dev/chatgpt-telegram-bot/en/latest/)

Enough words, let's launch your personal ChatGPT Telegram bot. 🚀

## Preparing

* You must have [Docker installed](https://docs.docker.com/engine/install/). You can run the project both on the PC and on the server.
* You must [register a bot in Telegram](https://t.me/BotFather) and get an access token.
* You also need [ChatGPT access token](https://platform.openai.com/account/api-keys).
* You must know your telegramId. [You can find it out here.](https://t.me/myidbot).

## Docker Run

The following platforms are supported: linux/amd64,linux/arm64/v8

``` bash
docker run -it --name chatgpt-telegram-bot \
    --env TELEGRAM_BOT_TOKEN= \
    --env TELEGRAM_BOT_USERNAME= \
    --env TELEGRAM_PERSON_ID= \
    --env CHAT_GPT_TOKEN= \
    upagge/chatgpt-telegram-bot:develop
```

`TELEGRAM_BOT_USERNAME` - Specify a name with the ending bot here, not a public name.

### Telegram Proxy
If you have Telegram blocked, you can specify proxy settings to connect.

``` bash   
docker run -it --name chatgpt-telegram-bot \
    --env TELEGRAM_BOT_TOKEN= \
    --env TELEGRAM_BOT_USERNAME= \
    --env TELEGRAM_PERSON_ID= \
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
      CHAT_GPT_TOKEN: ${CHAT_GPT_TOKEN}
```

``` text
TELEGRAM_BOT_TOKEN=
TELEGRAM_BOT_USERNAME=
TELEGRAM_PERSON_ID=
CHAT_GPT_TOKEN=
```