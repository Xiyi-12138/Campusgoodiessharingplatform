package com.example.campusgoodiessharingplatform;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.campusgoodiessharingplatform.model.AppNotification;
import com.example.campusgoodiessharingplatform.model.Charge;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class MessagesFragment extends BaseFragment {
    private View root;
    private MaterialButton received;
    private MaterialButton sent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_messages, container, false);
        renderNotifications();
        return root;
    }

    private void renderNotifications() {
        TextView title = root.findViewById(R.id.messages_title);
        MaterialButton readAll = root.findViewById(R.id.messages_read_all);
        MaterialButton back = root.findViewById(R.id.messages_back);
        received = root.findViewById(R.id.messages_received);
        sent = root.findViewById(R.id.messages_sent);
        LinearLayout list = root.findViewById(R.id.messages_list);
        title.setText("\u4fe1\u606f");
        readAll.setVisibility(View.VISIBLE);
        back.setVisibility(View.GONE);
        updateTabState(null);
        readAll.setOnClickListener(v -> call(api().readAll(currentUser().id), x -> renderNotifications()));
        back.setOnClickListener(v -> renderNotifications());
        received.setOnClickListener(v -> showReceivedCharges());
        sent.setOnClickListener(v -> showSentCharges());
        call(api().notifications(1, 50, currentUser().id), p -> {
            list.removeAllViews();
            if (p.list == null || p.list.isEmpty()) list.addView(empty("\u6682\u65e0\u4fe1\u606f"));
            else for (AppNotification n : p.list) {
                View card = simpleCard((Boolean.TRUE.equals(n.isRead) ? "" : "\u672a\u8bfb | ") + safe(n.content), safe(n.time));
                card.setOnClickListener(v -> call(api().readNotification(n.id), x -> renderNotifications()));
                list.addView(card);
            }
        }, error -> {
            list.removeAllViews();
            list.addView(empty("信息接口不可用，请启动 Campusgoodiessharingplatform/backend/springboot 后端\n" + error));
        });
    }

    private void showReceivedCharges() {
        updateTabState(received);
        TextView title = root.findViewById(R.id.messages_title);
        MaterialButton readAll = root.findViewById(R.id.messages_read_all);
        MaterialButton back = root.findViewById(R.id.messages_back);
        title.setText("\u6536\u5230\u7684\u4ea4\u6362\u7533\u8bf7");
        readAll.setVisibility(View.GONE);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(v -> renderNotifications());
        LinearLayout list = root.findViewById(R.id.messages_list);
        call(api().chargePage(1, 50, null, currentUser().id, null), p -> {
            list.removeAllViews();
            if (p.list == null || p.list.isEmpty()) list.addView(empty("\u6682\u65e0\u7533\u8bf7"));
            else for (Charge charge : p.list) list.addView(receivedChargeCard(charge));
        });
    }

    private void showSentCharges() {
        updateTabState(sent);
        TextView title = root.findViewById(R.id.messages_title);
        MaterialButton readAll = root.findViewById(R.id.messages_read_all);
        MaterialButton back = root.findViewById(R.id.messages_back);
        title.setText("\u6211\u7684\u4ea4\u6362\u7533\u8bf7");
        readAll.setVisibility(View.GONE);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(v -> renderNotifications());
        LinearLayout list = root.findViewById(R.id.messages_list);
        call(api().chargePage(1, 50, currentUser().id, null, null), p -> {
            list.removeAllViews();
            if (p.list == null || p.list.isEmpty()) list.addView(empty("\u6682\u65e0\u7533\u8bf7"));
            else for (Charge charge : p.list) list.addView(sentChargeCard(charge));
        });
    }

    private View receivedChargeCard(Charge charge) {
        LinearLayout card = card();
        card.addView(text(safe(charge.itemName), 17, true));
        card.addView(text("\u7533\u8bf7\u4eba: " + safe(charge.userName) + " | \u72b6\u6001: " + safe(charge.status), 13, false));
        card.addView(text("\u4ea4\u6362\u7269\u54c1: " + safe(charge.content), 14, false));
        card.addView(text("\u7406\u7531: " + safe(charge.remark), 14, false));
        if (STATUS_PASS.equals(charge.status)) card.addView(text("\u5730\u70b9: " + safe(charge.location) + " | \u65f6\u95f4: " + safe(charge.shareTime), 13, false));
        if (STATUS_REJECT.equals(charge.status)) card.addView(text("\u62d2\u7edd\u7406\u7531: " + safe(charge.reason), 13, false));
        if (STATUS_PENDING.equals(charge.status)) {
            LinearLayout actions = row();
            MaterialButton approve = button("\u901a\u8fc7");
            MaterialButton reject = outlineButton("\u62d2\u7edd");
            actions.addView(approve, new LinearLayout.LayoutParams(0, dp(42), 1));
            actions.addView(reject, new LinearLayout.LayoutParams(0, dp(42), 1));
            card.addView(actions, topLp(8));
            approve.setOnClickListener(v -> showApproveChargeDialog(charge));
            reject.setOnClickListener(v -> showRejectChargeDialog(charge));
        }
        return card;
    }

    private View sentChargeCard(Charge charge) {
        LinearLayout card = card();
        card.addView(text(safe(charge.itemName), 17, true));
        card.addView(text("\u7269\u54c1\u4e3b\u4eba: " + safe(charge.itemUserName) + " | \u72b6\u6001: " + safe(charge.status), 13, false));
        card.addView(text("\u6211\u63d0\u4f9b: " + safe(charge.content), 14, false));
        card.addView(text("\u7406\u7531: " + safe(charge.remark), 14, false));
        if (STATUS_PASS.equals(charge.status)) card.addView(text("\u4ea4\u6362\u5730\u70b9: " + safe(charge.location) + "\n\u4ea4\u6362\u65f6\u95f4: " + safe(charge.shareTime), 13, false));
        if (STATUS_REJECT.equals(charge.status)) card.addView(text("\u62d2\u7edd\u7406\u7531: " + safe(charge.reason), 13, false));
        return card;
    }

    private void showApproveChargeDialog(Charge charge) {
        LinearLayout form = formLayout();
        EditText location = input("\u4ea4\u6362\u5730\u70b9");
        TextView selectedTime = text("\u672a\u9009\u62e9\u4ea4\u6362\u65f6\u95f4", 14, false);
        MaterialButton pickTime = outlineButton("\u9009\u62e9\u4ea4\u6362\u65f6\u95f4");
        final String[] shareTime = {""};
        pickTime.setOnClickListener(v -> showDateTimePicker(value -> {
            shareTime[0] = value;
            selectedTime.setText(value);
        }));
        form.addView(location);
        form.addView(pickTime, topLp(8));
        form.addView(selectedTime, topLp(6));
        new AlertDialog.Builder(requireContext()).setTitle("\u901a\u8fc7\u7533\u8bf7").setView(form)
                .setNegativeButton("\u53d6\u6d88", null)
                .setPositiveButton("\u786e\u5b9a", (d, w) -> {
                    if (location.getText().toString().trim().isEmpty() || shareTime[0].isEmpty()) {
                        toast("\u4ea4\u6362\u5730\u70b9\u548c\u65f6\u95f4\u4e0d\u80fd\u4e3a\u7a7a");
                        return;
                    }
                    Map<String, Object> body = new HashMap<>();
                    body.put("id", charge.id);
                    body.put("status", STATUS_PASS);
                    body.put("location", location.getText().toString().trim());
                    body.put("shareTime", shareTime[0]);
                    call(api().updateCharge(body), x -> { toast("\u5df2\u901a\u8fc7"); showReceivedCharges(); });
                }).show();
    }

    private void showRejectChargeDialog(Charge charge) {
        EditText reason = input("\u62d2\u7edd\u7406\u7531");
        new AlertDialog.Builder(requireContext()).setTitle("\u62d2\u7edd\u7533\u8bf7").setView(reason)
                .setNegativeButton("\u53d6\u6d88", null)
                .setPositiveButton("\u786e\u5b9a", (d, w) -> {
                    String value = reason.getText().toString().trim();
                    if (value.isEmpty()) {
                        toast("\u62d2\u7edd\u7406\u7531\u4e0d\u80fd\u4e3a\u7a7a");
                        return;
                    }
                    Map<String, Object> body = new HashMap<>();
                    body.put("id", charge.id);
                    body.put("status", STATUS_REJECT);
                    body.put("reason", value);
                    call(api().updateCharge(body), x -> { toast("\u5df2\u62d2\u7edd"); showReceivedCharges(); });
                }).show();
    }

    private void showDateTimePicker(Consumer<String> selected) {
        Calendar now = Calendar.getInstance();
        new android.app.DatePickerDialog(requireContext(), (datePicker, year, month, day) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(Calendar.YEAR, year);
            chosen.set(Calendar.MONTH, month);
            chosen.set(Calendar.DAY_OF_MONTH, day);
            new android.app.TimePickerDialog(requireContext(), (timePicker, hour, minute) -> {
                chosen.set(Calendar.HOUR_OF_DAY, hour);
                chosen.set(Calendar.MINUTE, minute);
                chosen.set(Calendar.SECOND, 0);
                selected.accept(String.format(Locale.CHINA, "%04d-%02d-%02d %02d:%02d:%02d",
                        chosen.get(Calendar.YEAR), chosen.get(Calendar.MONTH) + 1, chosen.get(Calendar.DAY_OF_MONTH),
                        chosen.get(Calendar.HOUR_OF_DAY), chosen.get(Calendar.MINUTE), chosen.get(Calendar.SECOND)));
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateTabState(MaterialButton selected) {
        if (received == null || sent == null) return;
        setTabSelected(received, received == selected);
        setTabSelected(sent, sent == selected);
    }
}
