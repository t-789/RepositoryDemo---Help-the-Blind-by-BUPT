from flask import Flask, request, jsonify
from flask_cors import CORS
import os
from datetime import datetime
import re
import openai

app = Flask(__name__)
CORS(app)

# 直接在代码中设置DeepSeek API密钥和基础URL
# 注意：这种方式存在安全风险，生产环境中建议使用环境变量
DEEPSEEK_API_KEY = os.getenv('DEEPSEEK_API_KEY')  # 请替换为你的实际API密钥
DEEPSEEK_BASE_URL = "https://api.deepseek.com/v1"

if DEEPSEEK_API_KEY and DEEPSEEK_API_KEY != "your-deepseek-api-key-here":
    client = openai.OpenAI(api_key=DEEPSEEK_API_KEY, base_url=DEEPSEEK_BASE_URL)
else:
    client = None
    print("警告：使用模拟响应，因为未设置有效的DeepSeek API密钥")

# 简单的关键词处理器
class SimpleKeywordProcessor:
    def __init__(self):
        self.keywords = {
            '帮助': self._help_handler,
            '时间': self._time_handler,
            '清空': self._clear_handler,
            '笑话': self._joke_handler,
        }

    def process(self, text):
        text_lower = text.lower()
        for keyword, handler in self.keywords.items():
            if keyword in text_lower:
                return handler(text)
        return None

    def _help_handler(self, text):
        return "可用命令：帮助、时间、清空、笑话"

    def _time_handler(self, text):
        return f"当前时间：{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}"

    def _clear_handler(self, text):
        return "对话历史已清空（在完整版中实现）"

    def _joke_handler(self, text):
        jokes = ["为什么程序员总是分不清万圣节和圣诞节？因为 Oct 31 == Dec 25"]
        return jokes[0]

# 初始化处理器
keyword_processor = SimpleKeywordProcessor()

@app.route('/api/chat', methods=['POST'])
def chat():
    try:
        data = request.get_json()
        user_id = data.get('user_id', 'default')
        message = data.get('message', '').strip()

        if not message:
            return jsonify({
                "type": "error",
                "content": "消息不能为空",
                "timestamp": datetime.now().isoformat()
            })

        # 首先尝试关键词处理
        keyword_result = keyword_processor.process(message)
        if keyword_result:
            return jsonify({
                "type": "keyword",
                "content": keyword_result,
                "timestamp": datetime.now().isoformat()
            })

        # 如果没有匹配关键词且配置了DeepSeek API，则调用DeepSeek
        if client:
            try:
                response = client.chat.completions.create(
                    model="deepseek-chat",
                    messages=[
                        {"role": "system", "content": "你是一个乐于助人的助手"},
                        {"role": "user", "content": message}
                    ],
                    stream=False
                )
                ai_response = response.choices[0].message.content
                
                return jsonify({
                    "type": "ai",
                    "content": ai_response,
                    "timestamp": datetime.now().isoformat(),
                    "messageCount": 1
                })
            except Exception as e:
                print(f"调用DeepSeek API时出错: {e}")
                return jsonify({
                    "type": "error",
                    "content": f"AI服务错误: {str(e)}",
                    "timestamp": datetime.now().isoformat()
                }), 500
        else:
            # 如果没有配置API密钥，返回一个模拟的AI响应
            return jsonify({
                "type": "ai",
                "content": f"这是一个模拟响应。你说了：{message}",
                "timestamp": datetime.now().isoformat(),
                "messageCount": 1
            })

    except Exception as e:
        return jsonify({
            "type": "error",
            "content": f"服务错误: {str(e)}",
            "timestamp": datetime.now().isoformat()
        }), 500

@app.route('/api/deepseek', methods=['POST'])
def deepseek_chat():
    """专门用于直接与DeepSeek通信的端点"""
    try:
        if not client:
            return jsonify({
                "type": "error",
                "content": "DeepSeek API未配置",
                "timestamp": datetime.now().isoformat()
            }), 500
            
        data = request.get_json()
        prompt = data.get('prompt', '').strip()
        
        if not prompt:
            return jsonify({
                "type": "error",
                "content": "prompt不能为空",
                "timestamp": datetime.now().isoformat()
            })

        # 直接调用DeepSeek API
        response = client.chat.completions.create(
            model="deepseek-chat",
            messages=[
                {"role": "user", "content": prompt}
            ],
            stream=False
        )
        ai_response = response.choices[0].message.content
        
        return jsonify({
            "type": "ai",
            "content": ai_response,
            "timestamp": datetime.now().isoformat()
        })

    except Exception as e:
        print(f"调用DeepSeek API时出错: {e}")
        return jsonify({
            "type": "error",
            "content": f"AI服务错误: {str(e)}",
            "timestamp": datetime.now().isoformat()
        }), 500

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "healthy"})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000, debug=True)