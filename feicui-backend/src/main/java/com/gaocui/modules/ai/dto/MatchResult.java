package com.gaocui.modules.ai.dto;

import java.util.List;

/**
 * AI 找货匹配的结构化返回, 供 LangChain4j AiServices 自动反序列化.
 */
public record MatchResult(boolean isJadeNeed, String reply, List<Long> productIds) {
}
