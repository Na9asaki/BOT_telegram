package startcoder;

import com.google.common.collect.ImmutableMap;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendSticker;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.json.JSONObject;
import java.util.Random;


import java.io.IOException;
import java.util.Map;

public class Bot {
    private final String BOT_TOKEN = "5033617674:AAEgNYTYLpHkAdROCPMdvSDPJFtSu1DbVx4";

    public String[] stikers = new String[] {
            "CAACAgIAAxkBAAM8Yay7Qh786NHjO45hc7r7P0uMR3YAAkAAAw01jxGfuJDQV5jTTyIE",
            "CAACAgIAAxkBAAM-Yay7RpfQlBRt8Vs0J44YwdBZ62oAAmEAAw01jxHv8v2EYRH4rCIE",
            "CAACAgIAAxkBAANAYay7R9c7wZUN2ZlRQLr4p94wlqAAAgMAAw01jxFm9FZGRsmn1yIE",
            "CAACAgIAAxkBAANCYay7R8ATtS46tO1mU7h1i_Rr_NYAAh8AAw01jxGVyODAZ_gBJCIE",
            "CAACAgIAAxkBAANEYay7SWWTgiF1J24zGghWkYHOh4oAAhgAAw01jxH-P6a4Q9-URiIE",
            "CAACAgIAAxkBAANGYay7WMUIQbEV1dzTsgri7LjuppUAAiMAAw01jxEqF_XTx_zLTyIE",
            "CAACAgIAAxkBAANIYay7W_tp0J6lQXlpHtza-TOhOgkAAiAAAw01jxF82aUP3BJglyIE",
            "CAACAgIAAxkBAANKYay7Xr3Qr5mhxHxHcYsW85ytKyAAAjAAAw01jxFzM7u4fPZLnCIE",
            "CAACAgIAAxkBAANMYay7Yc1WvVvaXw1onRc2iNXAlFkAAi8AAw01jxH4XrWaIoZNdSIE",
            "CAACAgIAAxkBAANOYay7aGHhONncviapis5vkrTBQycAAlMAAw01jxF8bT4WWrU0liIE",
            "CAACAgIAAxkBAANQYay7coPNLHrM6cyjpPPhEN9KM_kAAk4AAw01jxG-NEmbPY0OwCIE"
    };

    private final Map<String, String> map = ImmutableMap.of(
            "clear", "ясно",
            "partly-cloudy", "малооблачно",
            "cloudy", "облачно с прояснениями",
            "overcast", "пасмурно",
            "drizzle", "морось",
            "light-rain", "небольшой дождь",
            "rain", "дождь",
            "moderate-rain", "умеренно сильный дождь",
            "heavy-rain", "сильный дождь",
            "snow", "снег");

    Random random = new Random();

    TelegramBot bot = new TelegramBot(BOT_TOKEN);

    public void serve() {
        bot.setUpdatesListener(updates -> {
            updates.forEach(this::process);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void process(Update update) {
        Message msg = update.message();


        if(msg.text() != null && msg.text().equals("погода"))
            wheather(msg);
        else if(msg.entities() != null)
            handleMessage(msg);
        else if(msg.sticker() != null)
            bot.execute(new SendSticker(msg.chat().id(), stikers[random.nextInt(0, 11)]));
        else
            bot.execute(new SendMessage(msg.chat().id(), "Очень смешно, но Андрюшка не научил меня распозновать это)"));
    }

    private String[] parse(String[] ans) {
        final Content getResult;
        try {
            String WHEATHER_TOKEN = "e88cc288-7f85-4bdc-abaf-55479f6b7d6c";
            String url = "https://api.weather.yandex.ru/v2/informers?lat=55.42&lon=52.20&lang=ru_RU";
            getResult = Request.Get(url)
                    .setHeader("X-Yandex-API-Key", WHEATHER_TOKEN)
                    .execute().returnContent();
            String jsonString = getResult.asString();
            JSONObject obj = new JSONObject(jsonString);
            ans[0] = String.valueOf(obj.getJSONObject("fact").getInt("temp"));
            ans[1] = String.valueOf(obj.getJSONObject("fact").getInt("feels_like"));
            ans[2] = obj.getJSONObject("fact").getString("condition");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ans;
    }

    private void wheather(Message msg) {
        String[] ans = new String[3];
        long chatId = msg.chat().id();

        if(msg.text().equals("погода") || msg.text().equals("/wheather")) {

            ans = parse(ans);

            String response = "";
            for (int i=0;i<3;++i) {
                if(i==0)
                    response += "На улице " + ans[i] + "°\n";
                else if(i == 1)
                    response += "Ощущается как " + ans[i] + "°\n";
                else {
                    if(map.containsKey(ans[i]))
                        response += "Сегодня на улице: " + map.get(ans[i]) + "\n";
                    else
                        response += "Сегодня на улице: " + ans[i] + "\n";
                }
            }
            bot.execute(new SendMessage(chatId, response));
            bot.execute(new SendMessage(chatId, "Желаю успехов! ^ ^"));
            bot.execute(new SendSticker(chatId, stikers[random.nextInt(0, 11)]));
        }
    }

    private void handleMessage(Message msg) {
        switch (msg.text()) {
            case "/wheather":
                wheather(msg);
            case "/start":
                bot.execute(new SendMessage(msg.chat().id(), "Привет! Я сделан на Java! Могу рассказать прогноз погоды"));
        }
    }
}
