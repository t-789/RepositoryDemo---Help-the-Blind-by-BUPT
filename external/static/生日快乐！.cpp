#include <iostream>
#include <windows.h>
#include <string>
#include <ctime>
using namespace std;

// 全局：设置控制台颜色（0-15，对应黑、蓝、绿、红、紫、黄、浅灰、白等）
void setColor(int color) {
    HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
    SetConsoleTextAttribute(hConsole, color);
}

// 高级逐字打印：支持颜色+渐显（前半段加速、后半段平稳，避免生硬）
void printAdvanced(const string& text, int baseDelay = 40, int color = 7) {
    setColor(color);
    int len = text.size();
    for (int i = 0; i < len; i++) {
        cout << text[i];
        cout.flush();
        // 前1/3文字加速（营造“奔赴而来”的感觉），后2/3平稳
        int delay = (i < len/3) ? baseDelay/2 : baseDelay;
        Sleep(delay);
    }
    setColor(7); // 恢复默认白色
    cout << endl;
}

// 闪烁强调效果（关键祝福词突出）
void flashText(const string& text, int flashTimes = 3, int color1 = 14, int color2 = 7) {
    HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
    for (int i = 0; i < flashTimes; i++) {
        SetConsoleTextAttribute(hConsole, color1); // 高亮色（黄/红）
        cout << text;
        cout.flush();
        Sleep(300);
        SetConsoleTextAttribute(hConsole, color2); // 默认色
        cout << string(text.size(), ' '); // 清空当前文字
        cout.flush();
        Sleep(200);
    }
    SetConsoleTextAttribute(hConsole, color1);
    cout << text; // 最终停在高亮色
    SetConsoleTextAttribute(hConsole, 7);
    cout << endl;
}

// 升级爱心动画：左右轻微晃动+颜色渐变
void drawHeartAdvanced() {
    string heart[] = {
        "  ❤️❤️   ❤️❤️",
        "❤️❤️❤️❤️❤️❤️❤️",
        "❤️❤️❤️❤️❤️❤️❤️",
        "   ❤️❤️❤️❤️❤️  ",
        "     ❤️❤️❤️    ",
        "       ❤️      "
    };
    int colorSequence[] = {12, 13, 14}; // 红→粉→黄 渐变
    int colorIdx = 0;
    int offset = 0; // 晃动偏移量
    bool moveRight = true;

    for (int i = 0; i < 6; i++) { // 逐行绘制+晃动
        setColor(colorSequence[colorIdx % 3]);
        // 左右晃动：每行会轻微偏移1-2个空格
        cout << string(offset, ' ');
        printAdvanced(heart[i], 80, colorSequence[colorIdx % 3]);
        colorIdx++;
        // 切换晃动方向
        if (moveRight) offset++;
        else offset--;
        if (offset >= 2) moveRight = false;
        if (offset <= 0) moveRight = true;
        Sleep(150);
    }
    setColor(7);
}

// 进度条加载（替代简单文字加载，更直观）
void loadingBar() {
    setColor(10); // 绿色进度条
    cout << "\n🎁 正在加载专属生日祝福... [";
    cout.flush();
    for (int i = 0; i < 20; i++) {
        cout << "■";
        cout.flush();
        Sleep(80);
    }
    cout << "] 100%" << endl;
    setColor(7);
}

int main() {
    system("chcp 65001"); // 设置控制台编码为UTF-8，支持中文
    system("title 「限定浪漫」专属生日祝福 💌");
    system("mode con cols=40 lines=25"); // 固定窗口大小，避免排版错乱

    // 1. 高级加载界面
    setColor(11); // 浅蓝色欢迎语
    cout << "========================================" << endl;
    cout << "          🌟 生日祝福限定版 🌟          " << endl;
    cout << "========================================" << endl;
    setColor(7);
    loadingBar();
    Sleep(1000);

    // 2. 输入专属署名（交互感）
    string yourName, herName;
    setColor(13);
    cout << "\n请输入我的名字（小名就行）：";
    cin >> yourName;
    cout << "请输入你的名字（你的也一样）：";
    cin >> herName;
    setColor(7);
    Sleep(800);
    system("cls"); // 清空屏幕，营造沉浸式体验

    // 3. 核心祝福（彩色+渐显+闪烁强调）
    printAdvanced("\n✨ 致我藏在心底的 " + herName + " ✨", 60, 14); // 黄色标题
    Sleep(1000);

    printAdvanced("今天是属于你的特别日子", 50, 11); // 浅蓝色
    printAdvanced("我学了一些代码，写了点儿祝福给你 🎉", 50, 11);
    Sleep(600);
    printAdvanced("愿你的世界里：", 50, 11);
    printAdvanced("阳光温柔，晚风浪漫 🌙", 50, 13); // 粉色
    printAdvanced("三餐四季，平安喜乐 🥳", 50, 13);
    setColor(12); // 红色强调
    cout << "所有美好，都";
    flashText("如期而至", 2, 12, 7); // 闪烁“如期而至”
    setColor(7);
    Sleep(1200);

    // 4. 升级爱心动画
    printAdvanced("\n这颗心，只为你跳动：", 60, 12);
    drawHeartAdvanced();
    Sleep(1500);

    // 5. 专属告白（融入署名，更有温度）
    printAdvanced("\n其实，认识你之后 🥰", 50, 13);
    Sleep(800);
    printAdvanced("每个平凡的日子都多了一份期待", 50, 11);
    printAdvanced("每个早晨都有了早起的动力", 50, 11);
    printAdvanced("每次自习都有你的身影相伴", 50, 11);
    printAdvanced("当然，还有", 50, 11);
    Sleep(800);
    printAdvanced("很开心能陪你走过这一段时光", 50, 11);
    Sleep(800);
    printAdvanced("往后的日子，" + yourName + "想继续默默守护你 💌", 50, 12);
    Sleep(1200);

    // 6. 结尾高亮祝福
    setColor(14);
    cout << "\n" << string(15, '🎂') << endl;
    flashText("生日快乐！我最珍视的 " + herName + "～", 3, 14, 7); // 闪烁高亮
    cout << string(15, '🎂') << endl;
    setColor(7);
    printAdvanced("愿你永远眼里有光，笑里藏糖 🍬", 60, 13);

    // 7. 收尾停留（避免闪退）
    Sleep(8000);
    return 0;
}