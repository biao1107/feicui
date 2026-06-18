package com.gaocui.modules.ai.service;

import com.gaocui.modules.ai.dto.MatchResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 找货匹配 AI 接口 (LangChain4j AiServices 声明式).
 * prompt 工程化: 系统词与用户模板抽到 resources/prompt/*.txt, 便于独立维护/A-B.
 * 返回 MatchResult, 框架自动注入 schema 并解析 JSON, 无需手写 prompt 拼接与解析.
 */
public interface JadeMatchAssistant {

    @SystemMessage(fromResource = "prompt/match-system.txt")
    @UserMessage(fromResource = "prompt/match-user.txt")
    MatchResult match(@V("message") String message, @V("products") String products);
}
