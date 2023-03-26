package dev.struchkov.example.bot.unit;

import dev.struchkov.example.bot.conf.AppProperty;
import dev.struchkov.example.bot.util.Cmd;
import dev.struchkov.example.bot.util.UnitName;
import dev.struchkov.godfather.main.domain.annotation.Unit;
import dev.struchkov.godfather.main.domain.content.Attachment;
import dev.struchkov.godfather.main.domain.content.Mail;
import dev.struchkov.godfather.simple.domain.unit.AnswerText;
import dev.struchkov.godfather.telegram.domain.attachment.CommandAttachment;
import dev.struchkov.godfather.telegram.main.core.util.Attachments;
import dev.struchkov.godfather.telegram.starter.PersonUnitConfiguration;
import dev.struchkov.openai.context.OpenAIClient;
import dev.struchkov.openai.domain.response.account.TotalUsageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static dev.struchkov.godfather.simple.domain.BoxAnswer.boxAnswer;

@Component
@RequiredArgsConstructor
public class BalanceUnit implements PersonUnitConfiguration {

    private final AppProperty appProperty;
    private final OpenAIClient openAIClient;

    @Unit(value = UnitName.BALANCE, global = true)
    public AnswerText<Mail> help() {
        return AnswerText.<Mail>builder()
                .triggerCheck(
                        mail -> {
                            if (appProperty.getAdminTelegramIds().contains(mail.getFromPersonId())) {
                                final List<Attachment> attachments = mail.getAttachments();
                                final Optional<CommandAttachment> optCommand = Attachments.findFirstCommand(attachments);
                                if (optCommand.isPresent()) {
                                    final CommandAttachment command = optCommand.get();
                                    return Cmd.BALANCE.equals(command.getCommandType());
                                }
                            }
                            return false;
                        }
                )
                .answer(() -> {
                    final TotalUsageResponse totalUsage = openAIClient.getTotalUsageInThisMount();
                    final double balance = totalUsage.getTotalUsage() / 100;
                    return boxAnswer("Used in this month: $" + balance);
                })
                .build();
    }

}
