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
        title.setText("信息");
        readAll.setVisibility(View.VISIBLE);
        back.setVisibility(View.GONE);
        updateTabState(null);
        readAll.setOnClickListener(v -> call(api().readAll(currentUser().id), x -> renderNotifications()));
        back.setOnClickListener(v -> renderNotifications());
        received.setOnClickListener(v -> showReceivedCharges());
        sent.setOnClickListener(v -> showSentCharges());
        call(api().notifications(1, 50, currentUser().id), p -> {
            list.removeAllViews();
            if (p.list == null || p.list.isEmpty()) list.addView(empty("暂无信息"));
            else for (AppNotification n : p.list) {
                View card = simpleCard((Boolean.TRUE.equals(n.isRead) ? "" : "未读 | ") + safe(n.content), safe(n.time));
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
        title.setText("收到的交换申请");
        readAll.setVisibility(View.GONE);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(v -> renderNotifications());
        LinearLayout list = root.findViewById(R.id.messages_list);
        call(api().chargePage(1, 50, null, currentUser().id, null), p -> {
            list.removeAllViews();
            if (p.list == null || p.list.isEmpty()) list.addView(empty("暂无申请"));
            else for (Charge charge : p.list) list.addView(receivedChargeCard(charge));
        });
    }

    private void showSentCharges() {
        updateTabState(sent);
        TextView title = root.findViewById(R.id.messages_title);
        MaterialButton readAll = root.findViewById(R.id.messages_read_all);
        MaterialButton back = root.findViewById(R.id.messages_back);
        title.setText("我的交换申请");
        readAll.setVisibility(View.GONE);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(v -> renderNotifications());
        LinearLayout list = root.findViewById(R.id.messages_list);
        call(api().chargePage(1, 50, currentUser().id, null, null), p -> {
            list.removeAllViews();
            if (p.list == null || p.list.isEmpty()) list.addView(empty("暂无申请"));
            else for (Charge charge : p.list) list.addView(sentChargeCard(charge));
        });
    }

    private View receivedChargeCard(Charge charge) {
        LinearLayout card = card();
        card.addView(text(safe(charge.itemName), 17, true));
        card.addView(text("申请人: " + safe(charge.userName) + " | 状态: " + safe(charge.status), 13, false));
        card.addView(text("交换物品: " + safe(charge.content), 14, false));
        card.addView(text("理由: " + safe(charge.remark), 14, false));
        if (STATUS_PASS.equals(charge.status)) card.addView(text("地点: " + safe(charge.location) + " | 时间: " + safe(charge.shareTime), 13, false));
        if (STATUS_REJECT.equals(charge.status)) card.addView(text("拒绝理由: " + safe(charge.reason), 13, false));
        if (STATUS_PENDING.equals(charge.status)) {
            LinearLayout actions = row();
            MaterialButton approve = button("通过");
            MaterialButton reject = outlineButton("拒绝");
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
        card.addView(text("物品主人: " + safe(charge.itemUserName) + " | 状态: " + safe(charge.status), 13, false));
        card.addView(text("我提供: " + safe(charge.content), 14, false));
        card.addView(text("理由: " + safe(charge.remark), 14, false));
        if (STATUS_PASS.equals(charge.status)) card.addView(text("交换地点: " + safe(charge.location) + "\n交换时间: " + safe(charge.shareTime), 13, false));
        if (STATUS_REJECT.equals(charge.status)) card.addView(text("拒绝理由: " + safe(charge.reason), 13, false));
        return card;
    }

    private void showApproveChargeDialog(Charge charge) {
        LinearLayout form = formLayout();
        EditText location = input("交换地点");
        TextView selectedTime = text("未选择交换时间", 14, false);
        MaterialButton pickTime = outlineButton("选择交换时间");
        final String[] shareTime = {""};
        pickTime.setOnClickListener(v -> showDateTimePicker(value -> {
            shareTime[0] = value;
            selectedTime.setText(value);
        }));
        form.addView(location);
        form.addView(pickTime, topLp(8));
        form.addView(selectedTime, topLp(6));
        new AlertDialog.Builder(requireContext()).setTitle("通过申请").setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (d, w) -> {
                    if (location.getText().toString().trim().isEmpty() || shareTime[0].isEmpty()) {
                        toast("交换地点和时间不能为空");
                        return;
                    }
                    Map<String, Object> body = new HashMap<>();
                    body.put("id", charge.id);
                    body.put("status", STATUS_PASS);
                    body.put("location", location.getText().toString().trim());
                    body.put("shareTime", shareTime[0]);
                    call(api().updateCharge(body), x -> { toast("已通过"); showReceivedCharges(); });
                }).show();
    }

    private void showRejectChargeDialog(Charge charge) {
        EditText reason = input("拒绝理由");
        new AlertDialog.Builder(requireContext()).setTitle("拒绝申请").setView(reason)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (d, w) -> {
                    String value = reason.getText().toString().trim();
                    if (value.isEmpty()) {
                        toast("拒绝理由不能为空");
                        return;
                    }
                    Map<String, Object> body = new HashMap<>();
                    body.put("id", charge.id);
                    body.put("status", STATUS_REJECT);
                    body.put("reason", value);
                    call(api().updateCharge(body), x -> { toast("已拒绝"); showReceivedCharges(); });
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
