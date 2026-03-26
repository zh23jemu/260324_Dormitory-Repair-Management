from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] if '__file__' in globals() else Path(r'C:\Coding\260324_Dormitory-Repair-Management')


def rtf_escape(text: str) -> str:
    parts = []
    for ch in text:
        code = ord(ch)
        if ch == "\\":
            parts.append(r"\\")
        elif ch == "{":
            parts.append(r"\{")
        elif ch == "}":
            parts.append(r"\}")
        elif ch == "\n":
            parts.append(r"\par" + "\n")
        elif 32 <= code <= 126:
            parts.append(ch)
        else:
            signed = code if code < 32768 else code - 65536
            parts.append(rf"\u{signed}?")
    return "".join(parts)


title = "毕业设计（论文）中期检查表"
topic = "高校宿舍报修管理系统的设计与实现"

progress = (
    "本课题前期已完成业务调研、文献查阅和需求分析，结合任务书明确了学生、宿管人员、"
    "维修人员和系统管理员四类角色的业务边界与核心流程。"
    "\n"
    "在系统设计方面，已完成总体架构设计、工单状态流转设计、PC 管理端与移动端 Web "
    "学生端的终端划分、数据库表结构设计、接口清单整理和运行方案设计。"
    "\n"
    "在实现方面，已搭建 Spring Boot + SQLite 后端、Vue 3 管理端与学生端基础工程，"
    "完成登录认证、报修申请、审核分配、维修反馈、评价反馈、公告管理、宿舍管理、统计展示等主要模块的开发，"
    "并导入示例数据，系统已具备基本联调与演示条件。"
)

milestones = (
    "1. 完成任务书、开题阶段资料整理和相关文献阅读。"
    "\n"
    "2. 完成系统需求分析、总体设计、数据库设计和接口设计文档。"
    "\n"
    "3. 完成 SQLite 建表、初始化脚本与示例数据设计。"
    "\n"
    "4. 完成后端核心接口开发及角色权限控制。"
    "\n"
    "5. 完成 PC 管理端和移动端学生端主要页面开发与联调。"
    "\n"
    "6. 完成运行说明、演示账号和一键启动脚本整理。"
)

problems = (
    "1. 图片访问与上传路径曾存在静态资源映射不一致的问题，已通过统一上传目录、补充示例图片和排查接口返回路径逐步解决。"
    "\n"
    "2. 多角色联调过程中存在状态流转边界、文案展示和异常提示不统一的问题，已通过补充示例数据和完善详情页进行验证。"
    "\n"
    "3. 后续仍需继续加强异常处理、测试覆盖、界面细节优化和论文材料整理，计划通过集中联调、补充测试用例和演示脚本进一步完善系统。"
)

teacher_comment = (
    "该生能够按照毕业设计任务书的要求推进课题研究，前期调研、需求分析、总体设计和系统实现工作较为扎实，"
    "已完成阶段性目标，具备继续完善系统和撰写论文的基础。建议后续进一步加强系统测试、功能细节优化和论文总结，"
    "按时完成毕业设计（论文）任务。"
)

content = '{\\rtf1\\ansi\\ansicpg936\\deff0\n' \
          '{\\fonttbl{\\f0\\fnil\\fcharset134 SimSun;}{\\f1\\fnil Arial;}}\n' \
          '\\viewkind4\\uc1\n' + rf"""\pard\qc\b\f0\fs32 {rtf_escape(title)}\b0\par
\pard\sa120\fs24 {rtf_escape('题目名称：')} {rtf_escape(topic)}\par
{rtf_escape('学生姓名：待填写    专业班级：待填写    学号：待填写')}\par
\par
\b {rtf_escape('一、进度情况（查阅中外文文献资料、综合运用知识、研究方案设计、研究方法和手段运用等）说明')}\b0\par
{rtf_escape(progress)}\par
\par
\b {rtf_escape('二、阶段性成果')}\b0\par
{rtf_escape(milestones)}\par
\par
\b {rtf_escape('三、存在的主要问题及解决方法')}\b0\par
{rtf_escape(problems)}\par
\par
\b {rtf_escape('四、指导教师对学生在毕业设计（论文）中的纪律及毕业设计（论文）任务的完成进展等方面的评语')}\b0\par
{rtf_escape(teacher_comment)}\par
\par
{rtf_escape('分数：__________    指导教师签名：__________')}\par
}}
"""

for filename in ["中期检查_已完成.doc", "中期检查_已完成.rtf"]:
    (ROOT / filename).write_text(content, encoding='gbk')
    print(ROOT / filename)
