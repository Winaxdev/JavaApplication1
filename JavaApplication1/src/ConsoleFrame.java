import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template whois
 */
   

    
    
/**
 *
 * @author yyyvv
 */
public class ConsoleFrame extends javax.swing.JFrame {
    
    private boolean gameActive = false;
    private boolean inNotesMode = false;
    private List<String> notes = new ArrayList<>(); 
    private int secretNumber;
    private int attemptsLeft;
    private boolean isEnteringNoteText;
    private boolean waitingForWhoisInput = false;
    private boolean optBoolean(String json, String key, boolean defaultValue) {
    String pattern = "\"" + key + "\":\\s*(true|false)";
    java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
    java.util.regex.Matcher m = p.matcher(json);
    if (m.find()) {
        return Boolean.parseBoolean(m.group(1));
    }
    return defaultValue;
}
private void openAntiVirusForm() {
    try {
        AntiVirus antiVirusForm = new AntiVirus();
        antiVirusForm.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                ConsoleFrame.this.setVisible(true);
            }
        });
        this.setVisible(false);
        antiVirusForm.setVisible(true);
        
    } catch (Exception e) {
        printToConsole("Ошибка открытия антивируса: " + e.getMessage());
    }
}
private String optString(String json, String key, String defaultValue) {
    String pattern = "\"" + key + "\":\\s*\"([^\"]+)\"";
    java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
    java.util.regex.Matcher m = p.matcher(json);
    if (m.find()) {
        return m.group(1);
    }
    return defaultValue;
}
     private void handleOsintNumber(String number) {
    new SwingWorker<Void, String>() {
        @Override
        protected Void doInBackground() {
            try {
                if (!number.matches("^7\\d{10}$")) {
                    publish("Номер должен быть в формате 7XXXXXXXXXX (11 цифр)");
                    return null;
                }

                String apiResponse = getPhoneInfoFromAPI(number);    
                
                boolean valid = optBoolean(apiResponse, "valid", false);
                String country = optString(apiResponse, "country_name", "Н/Д");
                String carrier = optString(apiResponse, "carrier", "Н/Д");
                String lineType = optString(apiResponse, "line_type", "Н/Д");
                String location = optString(apiResponse, "location", "Н/Д");

                String result = "\n=== Результат проверки ===\n"
                        + "Страна: " + country + "\n"
                        + "Оператор: " + carrier + "\n"
                        + "Тип линии: " + lineType + "\n"
                        + "Локация: " + location + "\n"
                        + "Валидный: " + valid;

                publish(result);

            } catch (Exception e) {
                publish("Ошибка: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void process(List<String> chunks) {
            for (String msg : chunks) {
                printToConsole(msg);
            }
        }
    }.execute();
}
     private void handleDorksCommand(String text) {
    if (text.isEmpty()) {
        printToConsole("Введите текст для генерации dorks: dorks ваш_текст");
        return;
    }

    List<String> dorks = generateGoogleDorks(text);
    printToConsole("\n=== Сгенерированные Google Dorks ===\n");
    for (String dork : dorks) {
        printToConsole(dork);
    }
}

private List<String> generateGoogleDorks(String text) {
    List<String> dorks = new ArrayList<>();
    String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);

    
    dorks.add("intext:\"" + text + "\"");
    dorks.add("inurl:\"" + text + "\"");
    dorks.add("intitle:\"" + text + "\"");
    dorks.add("filetype:pdf \"" + text + "\"");
    dorks.add("site:example.com \"" + text + "\"");
    
    
    dorks.add("intext:\"" + text + "\" filetype:log");
    dorks.add("inurl:\"" + text + "\" intitle:\"index of\"");
    dorks.add("\"" + text + "\" password filetype:xls");
    dorks.add("\"" + text + "\" login filetype:sql");
    dorks.add("site:gov.* \"" + text + "\"");

    return dorks;
}
private void handleShutdownCommand() {
    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Вы уверены, что хотите выключить компьютер?",
        "Подтверждение выключения",
        JOptionPane.YES_NO_OPTION
    );

    if (confirm == JOptionPane.YES_OPTION) {
        new Thread(() -> {
            try {
                String shutdownCommand;
                String os = System.getProperty("os.name").toLowerCase();

                if (os.contains("win")) {
                    shutdownCommand = "shutdown /s /t 0";
                } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                    shutdownCommand = "sudo shutdown -h now";
                } else {
                    printToConsole("Неподдерживаемая операционная система");
                    return;
                }

                Runtime.getRuntime().exec(shutdownCommand);
                printToConsole("Система будет выключена...");

            } catch (Exception e) {
                printToConsole("Ошибка выключения: " + e.getMessage());
                printToConsole("Требуются права администратора/root!");
            }
        }).start();
    }
}

    private String getPhoneInfoFromAPI(String phoneNumber) {
        String apiKey = "num_live_rD1hMQ3ilrHSUQmLqRWT3idLtvaHlnFcKH5snM2n";
        try {
            URL url = new URL("https://api.numlookupapi.com/v1/validate/" + phoneNumber + "?apikey=" + apiKey);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                }
            }
            return "{\"error\":\"Ошибка сервера: " + responseCode + "\"}";
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
        
    
     private void printToConsole(String message) {
             jTextArea1.append(message + "\n");
        }
    private void showCommands() {
        printToConsole("Доступные команды:");
        printToConsole("1. back - вернуться на слайд с играми");
        printToConsole("2. exit - выход из системы");
        printToConsole("3. game - начать игру 'Угадай число'");
        printToConsole("4. notes - работа с заметками");
        printToConsole("5. getip - показать IP адрес");
        printToConsole("6. help - показать этот список команд");
        printToConsole("7. whois - Проверить IP адрес");
        printToConsole("8. OSINTnumber [номер] - информация по номеру телефона(Вводить без +)");
        printToConsole("9. dorks [текст] - генерация Google Dorks, можно сразу вводить запрос, он выдаст дорки");
        printToConsole("10. shutdown - выключение компьютера (требует прав администратора)");
        printToConsole("11. av(AV) - открыть антивирус-форму");
    }

    /**
     * Creates new form ConsoleFrame shutdown getip
     */
    public ConsoleFrame() {
        initComponents();
        showCommands();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor. main
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTextArea1.setEditable(false);
        jTextArea1.setBackground(new java.awt.Color(0, 0, 0));
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        jTextArea1.setForeground(new java.awt.Color(255, 255, 255));
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);
        setResizable(false);
        setMaximumSize(getPreferredSize());

        jTextField1.setBackground(new java.awt.Color(0, 0, 102));
        jTextField1.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        jTextField1.setForeground(new java.awt.Color(204, 204, 204));
        jTextField1.setText("Поле ввода текста");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 849, Short.MAX_VALUE)
            .addComponent(jTextField1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        String input = jTextField1.getText().trim();
        jTextField1.setText("");      

          if (input.startsWith("OSINTnumber ")) {
            String number = input.substring("OSINTnumber ".length()).trim();
            if (number.isEmpty()) {
                printToConsole("Введите номер после команды: OSINTnumber 79123456789");
            } else {
                handleOsintNumber(number);
            }
            return;
        }
        if (waitingForWhoisInput) {
    String query = input;
    waitingForWhoisInput = false;
    performWhoisLookup(query);
    return;
        }      

        if (inNotesMode) {
            handleNotesInput(input);
            return;
        }

        if (gameActive) {
            handleGameInput(input);
            return;
        }

        switch (input.toLowerCase()) {
            case "back":
                new Menu().setVisible(true);
                this.dispose();
                break;
            case "exit":
                new Login().setVisible(true);
                this.dispose();
                break;
            case "game":
                startGame();
                break;
            case "notes":
                inNotesMode = true;
                printToConsole("Введите 1 для просмотра заметки или 2 для новой заметки:");
                break;
            case "getip":
                showIpAddress();
                break;
            case "help":
                showCommands();
                break;
                case "av":
    jTextArea1.setForeground(Color.RED);
    jTextArea1.setFont(jTextArea1.getFont().deriveFont(Font.BOLD));
    printToConsole("В разработке! Ожидайте обновления(никогда).");
    jTextArea1.setForeground(Color.WHITE);
    jTextArea1.setFont(jTextArea1.getFont().deriveFont(Font.PLAIN));
    break; //openAntiVirusForm();
            case "whois":
    printToConsole("Введите IP-адрес или домен для проверки:");
    waitingForWhoisInput = true;
    break;
            case "shutdown":
                handleShutdownCommand();
                break;
            default:
                printToConsole("Неизвестная команда. Введите 'help' для списка команд.");
                case "dorks":
    handleDorksCommand(input.replaceFirst("dorks", "").trim());
    break;
        }
    }//GEN-LAST:event_jTextField1ActionPerformed

    /**
     * @param args the command line arguments case 
     */
    public static void main(String args[]) {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
    try {
        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                javax.swing.UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
    } catch (Exception ex) {
        java.util.logging.Logger.getLogger(ConsoleFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
        new ConsoleFrame().setVisible(true); 
        }
    });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

        private void startGame() {
            secretNumber = (int)(Math.random() * 6) + 1;
            attemptsLeft = 2;
            gameActive = true;
            printToConsole("Угадай число от 1 до 6 за 2 попытки");
        }     

        private void handleGameInput(String input) {
            try {
        int guess = Integer.parseInt(input);
        if (guess < 1 || guess > 6) {
            printToConsole("Введите число от 1 до 6");
            return;
        }
        attemptsLeft--;
        if (guess == secretNumber) {
            printToConsole("Ты угадал!");
            gameActive = false;
        } else {
            if (attemptsLeft > 0) {
                printToConsole("Попробуйте еще раз. Осталось попыток: " + attemptsLeft);
            } else {
                printToConsole("Ты проиграл! Загаданное число было: " + secretNumber);
                gameActive = false;
            }
        }
    } catch (NumberFormatException e) {
        printToConsole("Пожалуйста, введите число");
    }
        }

        private void handleNotesInput(String input) {
        if (isEnteringNoteText) {
            String noteText = input.trim();
            if (noteText.isEmpty()) {
                printToConsole("Заметка не может быть пустой");
            } else {
                notes.add(noteText); 
                printToConsole("Заметка сохранена! Хотите добавить ещё? 1 - Просмотреть, 2=3, 3 - Начать заново.");
            }
            isEnteringNoteText = false;
            return;
        }

        switch (input) {
            case "1":
                printNotes(); 
                inNotesMode = false;
                break;
            case "2":
                printToConsole("Введите текст заметки:");
                isEnteringNoteText = true;
                break;
            case "3":
                printToConsole("Введите текст новой заметки:");
                isEnteringNoteText = true;
                break;
            case "exit":
                inNotesMode = false;
                break;
                
            default:
                printToConsole("Неверный ввод. Введите '1', '2' или '3'.");
        }
    }

    private void printNotes() {
        if (notes.isEmpty()) {
            printToConsole("Заметок нет.");
        } else {
            printToConsole("Ваши заметки:");
            for (int i = 0; i < notes.size(); i++) {
                printToConsole((i + 1) + ". " + notes.get(i));
            }
        }
    }

    private void showIpAddress() {
        try {

        InetAddress localhost = InetAddress.getLocalHost();
        String ipAddress = localhost.getHostAddress();

        printToConsole("Ваш локальный IP-адрес: " + ipAddress);
        

        try {
            String externalIp = new java.util.Scanner(
                new java.net.URL("https://api.ipify.org").openStream(), 
                "UTF-8").useDelimiter("\\A").next();
            printToConsole("Ваш внешний IP-адрес: " + externalIp);
        } catch (Exception e) {
            printToConsole("Не удалось получить внешний IP-адрес");
        }
        
    } catch (UnknownHostException e) {
        printToConsole("Не удалось определить IP-адрес");
        printToConsole("Ошибка: " + e.getMessage());
    }
    }

    private void performWhoisLookup(String query) {
        printToConsole("Выполняется WHOIS-запрос для: " + query);
    SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
        @Override
        protected Void doInBackground() {
            try {
                String urlStr = query.matches("\\d+\\.\\d+\\.\\d+\\.\\d+") ?
                        "https://rdap.org/ip/" + query :
                        "https://rdap.org/domain/" + query;
                
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                
                if (connection.getResponseCode() == 200) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        
                        // Упрощенный парсинг JSON без сторонних библиотек
                        String formattedOutput = parseWhoisResponse(response.toString());
                        publish(formattedOutput);
                    }
                } else {
                    publish("Ошибка: сервер вернул код " + connection.getResponseCode());
                }
            } catch (Exception e) {
                publish("Ошибка WHOIS-запроса: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void process(List<String> chunks) {
            for (String res : chunks) {
                printToConsole(res);
            }
        }
        
        private String parseWhoisResponse(String json) {
            StringBuilder result = new StringBuilder();
            result.append("\n=== WHOIS информация ===\n");
            
            try {
                
                extractField(json, "name", "Имя", result);
                extractField(json, "handle", "Handle", result);
                extractField(json, "startAddress", "Начальный IP", result);
                extractField(json, "endAddress", "Конечный IP", result);
                extractField(json, "country", "Страна", result);
                extractField(json, "type", "Тип", result);
                
                
                extractOrganizationInfo(json, result);
                
                
                extractEvents(json, result);
                
            } catch (Exception e) {
                result.append("\nНе удалось полностью обработать ответ. Полный ответ:\n")
                      .append(json);
            }
            
            return result.toString();
        }
        
        private void extractField(String json, String fieldName, String displayName, StringBuilder result) {
            String pattern = "\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                result.append(displayName).append(": ").append(m.group(1)).append("\n");
            }
        }
        
        private void extractOrganizationInfo(String json, StringBuilder result) {
            
            String orgPattern = "\"fn\"\\s*,\\s*\\{\\s*\\}\\s*,\\s*\"text\"\\s*,\\s*\"([^\"]+)\"";
            java.util.regex.Pattern pOrg = java.util.regex.Pattern.compile(orgPattern);
            java.util.regex.Matcher mOrg = pOrg.matcher(json);
            if (mOrg.find()) {
                result.append("\nОрганизация: ").append(mOrg.group(1)).append("\n");
            }
            
            
            String addrPattern = "\"adr\"\\s*,\\s*\\{\\s*\"label\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern pAddr = java.util.regex.Pattern.compile(addrPattern);
            java.util.regex.Matcher mAddr = pAddr.matcher(json);
            if (mAddr.find()) {
                result.append("Адрес: ").append(mAddr.group(1).replace("\\n", "\n          ")).append("\n");
            }
            
            
            String phonePattern = "\"tel\"\\s*,\\s*\\{\\s*\"type\"\\s*:\\s*\"voice\"\\s*\\}\\s*,\\s*\"text\"\\s*,\\s*\"([^\"]+)\"";
            java.util.regex.Pattern pPhone = java.util.regex.Pattern.compile(phonePattern);
            java.util.regex.Matcher mPhone = pPhone.matcher(json);
            if (mPhone.find()) {
                result.append("Телефон: ").append(mPhone.group(1)).append("\n");
            }
            
            
            String emailPattern = "\"email\"\\s*,\\s*\\{\\s*\"type\"\\s*:\\s*\"[^\"]*\"\\s*\\}\\s*,\\s*\"text\"\\s*,\\s*\"([^\"]+)\"";
            java.util.regex.Pattern pEmail = java.util.regex.Pattern.compile(emailPattern);
            java.util.regex.Matcher mEmail = pEmail.matcher(json);
            if (mEmail.find()) {
                result.append("Email: ").append(mEmail.group(1)).append("\n");
            }
        }
        
        private void extractEvents(String json, StringBuilder result) {
            String eventPattern = "\"eventAction\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"eventDate\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern pEvent = java.util.regex.Pattern.compile(eventPattern);
            java.util.regex.Matcher mEvent = pEvent.matcher(json);
            
            boolean hasEvents = false;
            while (mEvent.find()) {
                if (!hasEvents) {
                    result.append("\nСобытия:\n");
                    hasEvents = true;
                }
                result.append("  ").append(mEvent.group(1))
                      .append(": ").append(mEvent.group(2)).append("\n");
            }
        }
    };
    worker.execute();
}

    private static class JSONObject {

        public JSONObject() {
        }

        private JSONObject(String apiResponse) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        private AbstractStringBuilder optString(String country_name, String нд) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        private String getString(String error) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        private boolean has(String error) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

   
        }

    private static class AbstractStringBuilder {

        public AbstractStringBuilder() {
        }
    }
    }

