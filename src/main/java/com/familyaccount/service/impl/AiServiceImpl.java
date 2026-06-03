package com.familyaccount.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.familyaccount.common.UserContext;
import com.familyaccount.config.AiConfigManager;
import com.familyaccount.config.AiProperties;
import com.familyaccount.dto.*;
import com.familyaccount.entity.Category;
import com.familyaccount.entity.Record;
import com.familyaccount.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiProperties aiProperties;
    private final AiConfigManager aiConfigManager;
    private final RestTemplate aiRestTemplate;
    private final RecordService recordService;
    private final CategoryService categoryService;
    private final FamilyService familyService;
    private final StatsService statsService;
    private final ObjectMapper objectMapper;

    /** 类别关键词别名表（用户口语 → 标准类别名） */
    private static final Map<String, String> CATEGORY_ALIASES = Map.ofEntries(
            Map.entry("午餐", "餐饮"), Map.entry("晚饭", "餐饮"), Map.entry("早饭", "餐饮"),
            Map.entry("早餐", "餐饮"), Map.entry("晚餐", "餐饮"), Map.entry("火锅", "餐饮"),
            Map.entry("外卖", "餐饮"), Map.entry("买菜", "餐饮"), Map.entry("吃饭", "餐饮"),
            Map.entry("零食", "餐饮"), Map.entry("水果", "餐饮"), Map.entry("饮料", "餐饮"),
            Map.entry("打车", "交通"), Map.entry("地铁", "交通"), Map.entry("公交", "交通"),
            Map.entry("加油", "交通"), Map.entry("高铁", "交通"), Map.entry("火车", "交通"),
            Map.entry("机票", "交通"), Map.entry("停车", "交通"),
            Map.entry("买衣服", "购物"), Map.entry("淘宝", "购物"), Map.entry("京东", "购物"),
            Map.entry("网购", "购物"), Map.entry("日用品", "购物"),
            Map.entry("房租", "居住"), Map.entry("水电", "居住"), Map.entry("物业", "居住"),
            Map.entry("电费", "居住"), Map.entry("水费", "居住"), Map.entry("燃气", "居住"),
            Map.entry("看病", "医疗"), Map.entry("药", "医疗"), Map.entry("挂号", "医疗"),
            Map.entry("游戏", "娱乐"), Map.entry("电影", "娱乐"), Map.entry("KTV", "娱乐"),
            Map.entry("旅游", "娱乐"), Map.entry("门票", "娱乐"),
            Map.entry("学费", "教育"), Map.entry("培训", "教育"), Map.entry("书本", "教育"),
            Map.entry("话费", "通讯"), Map.entry("宽带", "通讯"), Map.entry("手机", "通讯"),
            Map.entry("红包收入", "红包"), Map.entry("抢红包", "红包"),
            Map.entry("发工资", "工资"), Map.entry("涨薪", "工资"), Map.entry("薪水", "工资"),
            Map.entry("副业", "兼职"), Map.entry("接单", "兼职"),
            Map.entry("股票", "理财"), Map.entry("基金", "理财"), Map.entry("利息", "理财"),
            Map.entry("礼金", "人情"), Map.entry("份子钱", "人情")
    );

    @Override
    public AiChatResponse chat(String userMessage) {
        // 1. 检查 AI 是否启用
        if (!aiProperties.isEnabled()) {
            return AiChatResponse.builder()
                    .role("ai")
                    .content("AI助手已被管理员禁用")
                    .intent("error")
                    .build();
        }
        if (aiConfigManager.getEffectiveApiKey() == null
                || aiConfigManager.getEffectiveApiKey().isBlank()) {
            return AiChatResponse.builder()
                    .role("ai")
                    .content("AI助手暂未配置 API Key。请在设置页「AI 配置」中配置，\n"
                            + "或在 JAR 同级目录创建 ai-config.properties 文件并写入 api-key=你的Key")
                    .intent("error")
                    .build();
        }

        // 2. 检查用户是否已加入家庭
        Long familyId = UserContext.getFamilyId();
        if (familyId == null) {
            return AiChatResponse.builder()
                    .role("ai")
                    .content("请先创建或加入一个家庭，我才能帮你记账哦~")
                    .intent("chat")
                    .build();
        }

        try {
            // 3. 获取上下文信息
            List<Category> categories = categoryService.listCategories(null);
            List<Map<String, Object>> members = familyService.getMembers(familyId);
            String currentMemberName = getCurrentMemberName(members);

            // 4. 构建 System Prompt
            String systemPrompt = buildSystemPrompt(categories, members, currentMemberName);

            // 5. 调用通义千问 API
            String aiJson = callAiApi(systemPrompt, userMessage);

            // 6. 解析 AI 返回的 JSON
            AiParsedIntent parsed = parseAiResponse(aiJson);

            // 7. 执行对应动作
            return executeIntent(parsed, categories, members, currentMemberName);

        } catch (RestClientException e) {
            log.error("AI API 调用失败", e);
            return AiChatResponse.builder()
                    .role("ai")
                    .content("AI服务响应超时，请稍后重试。")
                    .intent("error")
                    .build();
        } catch (Exception e) {
            log.error("AI 处理异常", e);
            return AiChatResponse.builder()
                    .role("ai")
                    .content("抱歉，处理您的消息时出错了：" + e.getMessage())
                    .intent("error")
                    .build();
        }
    }

    // ==================== 上下文构建 ====================

    /** 获取当前用户对应的家庭成员名称 */
    private String getCurrentMemberName(List<Map<String, Object>> members) {
        Long memberId = UserContext.getMemberId();
        if (memberId != null) {
            for (Map<String, Object> m : members) {
                if (memberId.equals(m.get("id"))) {
                    return (String) m.get("name");
                }
            }
        }
        // fallback：用用户名
        String username = UserContext.getUsername();
        return username != null ? username : "我";
    }

    /** 构建 System Prompt */
    private String buildSystemPrompt(List<Category> categories,
                                      List<Map<String, Object>> members,
                                      String currentMemberName) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个家庭记账助手。当前用户信息：\n");
        sb.append("- 用户名：").append(UserContext.getUsername()).append("\n");
        sb.append("- 用户ID：").append(UserContext.getUserId())
                .append("（这是标识账单所属用户的重要关键字，所有新增记录会自动绑定此用户ID）\n");
        sb.append("- 身份标签：").append(currentMemberName)
                .append(" - 用户说「我」就是指这个身份\n");
        sb.append("- 当前日期：").append(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("\n\n");

        // 列出可用类别
        sb.append("该家庭可用的收支类别：\n");
        for (Category c : categories) {
            sb.append(String.format("- %s %s (%s)\n",
                    c.getIcon() != null ? c.getIcon() : "📦",
                    c.getName(),
                    c.getType()));
        }
        sb.append("\n");

        // 列出家庭成员
        sb.append("家庭成员：\n");
        for (Map<String, Object> m : members) {
            sb.append("- ").append(m.get("name"));
            if (m.get("username") != null) {
                sb.append("（用户：").append(m.get("username")).append("）");
            }
            sb.append("\n");
        }
        sb.append("\n");

        // 规则说明
        sb.append("【你的任务】根据用户的自然语言输入，判断意图并返回严格JSON。\n\n");
        sb.append("【三种意图】\n");
        sb.append("1. add_record：记录一笔收支。JSON格式：\n");
        sb.append("{\"intent\":\"add_record\",\"type\":\"EXPENSE\",\"categoryName\":\"餐饮\",\"amount\":36.00,\"recordDate\":\"2026-06-01\",\"familyMember\":\"")
                .append(currentMemberName).append("\",\"note\":\"午餐\"}\n\n");
        sb.append("2. query：查询统计数据。JSON格式：\n");
        sb.append("{\"intent\":\"query\",\"queryType\":\"by_category\",\"queryPeriod\":\"month\",\"queryCategory\":\"餐饮\",\"queryMember\":null,\"queryScope\":\"personal\",\"note\":\"\"}\n");
        sb.append("  - queryType 怎么选：\n");
        sb.append("    * 用户问「XX花了多少」「XX消费」「XX支出」「XX费用」（提到了具体消费项目）→ 用 by_category\n");
        sb.append("    * 用户问「XX收入多少」「XX赚了多少」「XX发了多少」「XX进了多少」（问到收入项目）→ 也用 by_category\n");
        sb.append("    * 用户问「总共花了多少」「收支怎么样」「本月汇总」→ 用 summary\n");
        sb.append("    * 用户问「趋势」「每月变化」「这几个月」→ 用 monthly_trend\n");
        sb.append("  - queryCategory：从用户的消费项目中提取类别名，必须从上表中选。例：\n");
        sb.append("    * 「吃饭」→ 餐饮、「打车/交通」→ 交通、「买衣服/购物」→ 购物\n");
        sb.append("    * 「房租」→ 居住、「话费」→ 通讯、「看电影」→ 娱乐\n");
        sb.append("    * 「红包收入」「抢红包」→ 红包、「工资」「发工资」→ 工资、「兼职」「副业」→ 兼职、「理财收益」→ 理财\n");
        sb.append("    * 如果用户没有提到具体消费项目 → 填 null\n");
        sb.append("  - queryPeriod：从用户时间词推断：\"今天\"→day / \"本周\"→week / \"这个月\"→month / \"今年\"→year。默认 month。\n");
        sb.append("  - queryMember：只有当用户明确提到其他家庭成员时才填（如「爸爸吃饭花了多少」→queryMember=\"爸爸\"）。用户说「我」或不提→填 null。\n");
        sb.append("  - queryScope：\n");
        sb.append("    * 用户说「我们家」「家里」「全家」「家庭」（含家庭语言）→ 填 \"family\"（查全家人数据）\n");
        sb.append("    * 用户说「我」「我的」或不提→ 填 \"personal\" 或省略（系统默认只查本人）\n");
        sb.append("    * 示例：「我们家这个月交通花了多少」→ queryScope=\"family\"、queryCategory=\"交通\"\n");
        sb.append("    * 示例：「今天吃饭花了多少」→ queryScope=\"personal\"、queryCategory=\"餐饮\"\n\n");
        sb.append("3. chat：闲聊或无法处理。JSON格式：\n");
        sb.append("{\"intent\":\"chat\",\"reply\":\"你的回复内容\"}\n\n");
        sb.append("【重要规则】\n");
        sb.append("- 类别名必须从上表中选择，不要编造\n");
        sb.append("- 「块」就是「元」，金额只输出数字\n");
        sb.append("- 日期默认今天，不说明则不填\n");
        sb.append("- 用户说「我」时用 \"").append(currentMemberName).append("\"\n");
        sb.append("- 不指定成员时默认当前用户\"").append(currentMemberName).append("\"\n");
        sb.append("- ⚠️ 返回纯JSON，不要包含```json代码块标记，不要在JSON前后添加任何其他文字\n");
        sb.append("- ⚠️ 用户问「XX花了多少」时，默认只查他自己的账单，不包含其他家庭成员\n");
        sb.append("- ⚠️ 用户说「我们家」「家里」「全家」「家庭」时，必须设置 queryScope=\"family\"，查全家庭总账\n");

        return sb.toString();
    }

    // ==================== API 调用 ====================

    /** 调用通义千问 DashScope API */
    private String callAiApi(String systemPrompt, String userMessage) {
        String url = aiProperties.getBaseUrl() + "/chat/completions";

        // 构建请求体
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", aiProperties.getModel());
        requestBody.put("temperature", aiProperties.getTemperature());
        requestBody.put("max_tokens", aiProperties.getMaxTokens());

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userMessage));
        requestBody.put("messages", messages);

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + aiConfigManager.getEffectiveApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.debug("调用 AI API: {}", url);
        ResponseEntity<Map<String, Object>> response = aiRestTemplate.postForEntity(
                url, entity, (Class<Map<String, Object>>) (Class<?>) Map.class);

        if (response.getBody() == null) {
            throw new RuntimeException("AI API 返回为空");
        }

        // 提取 choices[0].message.content
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("AI API 没有返回 choices");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");
        log.debug("AI 返回内容: {}", content);
        return content;
    }

    // ==================== 响应解析 ====================

    /** 解析 AI 返回的 JSON */
    private AiParsedIntent parseAiResponse(String aiResponse) {
        String json = aiResponse.trim();
        // 去掉可能的 markdown 代码块标记
        if (json.startsWith("```")) {
            json = json.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
        }
        // 提取第一个 JSON 对象
        int start = json.indexOf('{');
        int end = json.lastIndexOf('}');
        if (start >= 0 && end > start) {
            json = json.substring(start, end + 1);
        }

        try {
            return objectMapper.readValue(json, AiParsedIntent.class);
        } catch (JsonProcessingException e) {
            log.warn("AI 返回 JSON 解析失败: {}", json, e);
            // 如果解析失败，当作闲聊处理
            AiParsedIntent fallback = new AiParsedIntent();
            fallback.setIntent("chat");
            fallback.setReply(aiResponse.length() > 500 ? aiResponse.substring(0, 500) : aiResponse);
            return fallback;
        }
    }

    // ==================== 意图执行 ====================

    /** 根据解析出的意图执行对应动作 */
    @SuppressWarnings("unchecked")
    private AiChatResponse executeIntent(AiParsedIntent parsed,
                                          List<Category> categories,
                                          List<Map<String, Object>> members,
                                          String currentMemberName) {
        String intent = parsed.getIntent();
        if (intent == null) {
            return AiChatResponse.builder()
                    .role("ai").content("我没理解您的意思，请再描述一下？")
                    .intent("chat").build();
        }

        return switch (intent) {
            case "add_record" -> executeAddRecord(parsed, categories, members, currentMemberName);
            case "query" -> executeQuery(parsed, categories);
            case "chat" -> AiChatResponse.builder()
                    .role("ai").content(parsed.getReply() != null ? parsed.getReply() : "好的")
                    .intent("chat").build();
            default -> AiChatResponse.builder()
                    .role("ai").content("收到！但我还不太理解，您可以试试说'今天午餐花了36块'这样的格式")
                    .intent("chat").build();
        };
    }

    /** 执行添加记录 */
    private AiChatResponse executeAddRecord(AiParsedIntent parsed,
                                             List<Category> categories,
                                             List<Map<String, Object>> members,
                                             String currentMemberName) {
        // 1. 匹配类别
        Category matchedCategory = matchCategory(parsed.getCategoryName(), categories);
        if (matchedCategory == null) {
            String categoryNames = categories.stream()
                    .map(c -> c.getIcon() + " " + c.getName())
                    .collect(Collectors.joining("、"));
            return AiChatResponse.builder()
                    .role("ai")
                    .content("抱歉，我没有找到\"" + parsed.getCategoryName() + "\"这个类别。\n可用的类别有：" + categoryNames + "\n请重新描述一下？")
                    .intent("chat")
                    .build();
        }

        // 2. 确定类型
        String type = parsed.getType();
        if (type == null || (!"INCOME".equalsIgnoreCase(type) && !"EXPENSE".equalsIgnoreCase(type))) {
            // 根据匹配到的类别类型推断
            type = matchedCategory.getType();
        } else {
            type = type.toUpperCase();
        }

        // 3. 金额
        if (parsed.getAmount() == null || parsed.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return AiChatResponse.builder()
                    .role("ai").content("请告诉我具体的金额？比如'午餐花了36块'")
                    .intent("chat").build();
        }

        // 4. 成员
        String memberName = parsed.getFamilyMember();
        if (memberName == null || memberName.isBlank()) {
            memberName = currentMemberName;
        }
        // 验证成员名是否存在
        String finalMemberName = memberName;
        boolean memberExists = members.stream()
                .anyMatch(m -> finalMemberName.equals(m.get("name")));
        if (!memberExists) {
            memberName = currentMemberName; // fallback 到当前用户
        }

        // 5. 日期
        LocalDate recordDate = LocalDate.now();
        if (parsed.getRecordDate() != null && !parsed.getRecordDate().isBlank()) {
            try {
                recordDate = LocalDate.parse(parsed.getRecordDate());
            } catch (Exception e) {
                log.warn("日期解析失败: {}", parsed.getRecordDate());
            }
        }

        // 6. 创建记录
        Record record = Record.builder()
                .type(type)
                .categoryId(matchedCategory.getId())
                .amount(parsed.getAmount())
                .familyMember(memberName)
                .recordDate(recordDate)
                .note(parsed.getNote() != null ? parsed.getNote() : "")
                .build();

        Record saved = recordService.addRecord(record);

        // 7. 构建响应
        String typeLabel = "INCOME".equals(type) ? "收入" : "支出";
        String content = String.format("✅ 已记录%s：%s %s %.2f元，成员 %s，日期 %s",
                typeLabel,
                matchedCategory.getIcon() != null ? matchedCategory.getIcon() : "",
                matchedCategory.getName(),
                saved.getAmount(),
                saved.getFamilyMember(),
                saved.getRecordDate().format(DateTimeFormatter.ISO_LOCAL_DATE));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("record", Map.of(
                "id", saved.getId(),
                "type", saved.getType(),
                "categoryName", matchedCategory.getName(),
                "categoryIcon", matchedCategory.getIcon(),
                "amount", saved.getAmount().doubleValue(),
                "familyMember", saved.getFamilyMember(),
                "recordDate", saved.getRecordDate().toString(),
                "note", saved.getNote()
        ));

        return AiChatResponse.builder()
                .role("ai").content(content).intent("add_record").data(data).build();
    }

    /** 执行查询统计 */
    @SuppressWarnings("unchecked")
    private AiChatResponse executeQuery(AiParsedIntent parsed, List<Category> categories) {
        String queryType = parsed.getQueryType() != null ? parsed.getQueryType() : "summary";
        String period = parsed.getQueryPeriod() != null ? parsed.getQueryPeriod() : "month";
        String queryCategory = parsed.getQueryCategory();

        // 智能推断：如果用户问了具体类别，但 AI 仍返回了 summary，自动切换为 by_category
        if ("summary".equals(queryType) && queryCategory != null && !queryCategory.isBlank()) {
            queryType = "by_category";
        }

        Map<String, Object> data = new LinkedHashMap<>();
        StringBuilder content = new StringBuilder();

        try {
            switch (queryType) {
                case "by_category" -> {
                    // 不管收入还是支出，都查询全部类型，后续按类别名匹配
                    String statsType = null;
                    // 判断是否为家庭范围查询（用户说「我们家」「全家」）
                    boolean isFamilyScope = "family".equals(parsed.getQueryScope());
                    Long filterUserId;
                    if (queryCategory != null && !queryCategory.isBlank()) {
                        // 具体类别查询：默认只查本人，除非明确说全家
                        filterUserId = isFamilyScope ? null : UserContext.getUserId();
                    } else {
                        filterUserId = null; // 全类别统计不限制用户
                    }
                    List<CategoryStatsVO> stats = statsService.getByCategory(period, statsType, null, null, filterUserId);

                    if (queryCategory != null && !queryCategory.isBlank()) {
                        // === 精准查询：返回该类别消费/收入 ===
                        CategoryStatsVO matched = matchCategoryStats(queryCategory, stats);
                        // 根据范围选择人称
                        String who = isFamilyScope ? "你们家" : "你";
                        // 查找匹配的类别，判断是收入还是支出
                        String catType = findCategoryType(queryCategory, categories);
                        boolean isIncome = "INCOME".equalsIgnoreCase(catType);
                        String actionLabel = isIncome ? "共收入" : "共支出";
                        if (matched != null) {
                            data.put("categories", List.of(matched));
                            content.append(matched.getIcon() != null ? matched.getIcon() : "")
                                    .append(" ").append(who).append(getPeriodLabel(period))
                                    .append("「").append(matched.getCategoryName()).append("」")
                                    .append(actionLabel).append(" ").append(matched.getTotal().toString())
                                    .append(" 元");
                            if (matched.getCount() != null && matched.getCount() > 0) {
                                content.append("，计 ").append(matched.getCount()).append(" 笔");
                            }
                        } else {
                            content.append("📊 ").append(who).append(getPeriodLabel(period))
                                    .append("没有「").append(queryCategory).append("」类别的记录哦~");
                        }
                    } else {
                        // === 全类别统计：返回所有类别 ===
                        data.put("categories", stats);
                        content.append("📊 ").append(getPeriodLabel(period)).append("按类别统计：\n");
                        BigDecimal total = BigDecimal.ZERO;
                        for (CategoryStatsVO s : stats) {
                            total = total.add(s.getTotal());
                        }
                        for (CategoryStatsVO s : stats) {
                            content.append(String.format("%s %s：%.2f元（%.1f%%）\n",
                                    s.getIcon() != null ? s.getIcon() : "", s.getCategoryName(),
                                    s.getTotal(), s.getPercentage()));
                        }
                        if (!stats.isEmpty()) {
                            content.append(String.format("合计：%.2f元", total));
                        }
                    }
                }
                case "monthly_trend" -> {
                    List<MonthlyTrendVO> trends = statsService.getMonthlyTrend(6, null, null, null);
                    data.put("trends", trends);
                    content.append("📈 近6个月趋势：\n");
                    for (MonthlyTrendVO t : trends) {
                        content.append(String.format("%s：收入 %.0f | 支出 %.0f | 结余 %.0f\n",
                                t.getMonth(), t.getIncome(), t.getExpense(),
                                t.getIncome().subtract(t.getExpense())));
                    }
                }
                default -> {
                    // summary：总体收支汇总
                    StatsSummaryVO summary = statsService.getSummary(period, null, null, null);
                    data.put("summary", Map.of(
                            "totalIncome", summary.getTotalIncome().doubleValue(),
                            "totalExpense", summary.getTotalExpense().doubleValue(),
                            "balance", summary.getBalance().doubleValue(),
                            "recordCount", summary.getRecordCount(),
                            "periodLabel", summary.getPeriodLabel()
                    ));
                    content.append(String.format("📊 %s：\n收入 %.2f 元 | 支出 %.2f 元 | 结余 %.2f 元 | 共 %d 笔",
                            summary.getPeriodLabel(),
                            summary.getTotalIncome(),
                            summary.getTotalExpense(),
                            summary.getBalance(),
                            summary.getRecordCount()));
                }
            }
        } catch (Exception e) {
            log.error("查询统计失败", e);
            return AiChatResponse.builder()
                    .role("ai").content("查询统计数据时出错：" + e.getMessage())
                    .intent("error").build();
        }

        return AiChatResponse.builder()
                .role("ai").content(content.toString()).intent("query").data(data).build();
    }

    // ==================== 辅助方法 ====================

    /** 模糊匹配类别：先精确匹配 → 包含匹配 → 别名表 */
    private Category matchCategory(String categoryName, List<Category> categories) {
        if (categoryName == null || categoryName.isBlank()) return null;

        // 1. 先查别名表
        String resolvedName = CATEGORY_ALIASES.getOrDefault(categoryName.trim(), categoryName.trim());

        // 2. 精确匹配
        for (Category c : categories) {
            if (c.getName().equals(resolvedName)) return c;
        }

        // 3. 包含匹配
        for (Category c : categories) {
            if (resolvedName.contains(c.getName()) || c.getName().contains(resolvedName))
                return c;
        }

        return null;
    }

    /** 查询类别的类型（INCOME/EXPENSE），用于判断回复措辞 */
    private String findCategoryType(String queryCategory, List<Category> categories) {
        if (queryCategory == null || queryCategory.isBlank() || categories == null) return null;
        // 先通过别名表解析
        String resolvedName = CATEGORY_ALIASES.getOrDefault(queryCategory.trim(), queryCategory.trim());
        // 精确匹配
        for (Category c : categories) {
            if (resolvedName.equals(c.getName())) return c.getType();
        }
        // 包含匹配
        for (Category c : categories) {
            if (resolvedName.contains(c.getName()) || c.getName().contains(resolvedName))
                return c.getType();
        }
        return null;
    }

    /** 模糊匹配统计结果中的类别：先精确匹配 → 包含匹配 → 别名表 */
    private CategoryStatsVO matchCategoryStats(String queryCategory, List<CategoryStatsVO> stats) {
        if (queryCategory == null || queryCategory.isBlank() || stats == null) return null;

        // 1. 先查别名表
        String resolvedName = CATEGORY_ALIASES.getOrDefault(queryCategory.trim(), queryCategory.trim());

        // 2. 精确匹配类别名
        for (CategoryStatsVO s : stats) {
            if (resolvedName.equals(s.getCategoryName())) return s;
        }

        // 3. 包含匹配
        for (CategoryStatsVO s : stats) {
            if (s.getCategoryName() != null && s.getCategoryName().contains(resolvedName))
                return s;
        }

        return null;
    }

    private String getPeriodLabel(String period) {
        return switch (period != null ? period : "month") {
            case "year" -> "今年";
            case "week" -> "本周";
            case "day" -> "今天";
            default -> "本月";
        };
    }
}
