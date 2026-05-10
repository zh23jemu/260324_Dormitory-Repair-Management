from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


# 重新绘制“图4_系统E-R图.png”。
# 目标：保留本项目真实实体关系，同时让实体、关系菱形、线段和文字标注互相避让，
# 避免之前出现的文字与图形/连线重叠问题。
OUT = Path("docs/project-doc-assets/reworked-diagrams/图4_系统E-R图.png")
W, H = 1600, 1050


def load_font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    candidates = ["C:/Windows/Fonts/msyhbd.ttc"] if bold else []
    candidates += [
        "C:/Windows/Fonts/msyh.ttc",
        "C:/Windows/Fonts/simhei.ttf",
        "C:/Windows/Fonts/simsun.ttc",
    ]
    for item in candidates:
        if Path(item).exists():
            return ImageFont.truetype(item, size)
    return ImageFont.load_default()


img = Image.new("RGB", (W, H), "white")
d = ImageDraw.Draw(img)
FT = load_font(18)
FT_SM = load_font(15)
FT_XS = load_font(13)


def text_size(text: str, font: ImageFont.ImageFont):
    box = d.textbbox((0, 0), text, font=font)
    return box[2] - box[0], box[3] - box[1]


def center_text(box, text: str, font: ImageFont.ImageFont = FT):
    x1, y1, x2, y2 = box
    lines = text.split("\n")
    sizes = [text_size(line, font) for line in lines]
    total_h = sum(h for _, h in sizes) + (len(lines) - 1) * 5
    y = y1 + (y2 - y1 - total_h) / 2
    for line, (w, h) in zip(lines, sizes):
        d.text((x1 + (x2 - x1 - w) / 2, y), line, font=font, fill="black")
        y += h + 5


def rect(cx: int, cy: int, w: int, h: int, text: str):
    box = (cx - w // 2, cy - h // 2, cx + w // 2, cy + h // 2)
    d.rectangle(box, outline="black", width=2, fill="white")
    center_text(box, text, FT_SM)
    return box


def diamond(cx: int, cy: int, w: int, h: int, text: str):
    points = [(cx, cy - h // 2), (cx + w // 2, cy), (cx, cy + h // 2), (cx - w // 2, cy)]
    d.polygon(points, outline="black", fill="white")
    d.line(points + [points[0]], fill="black", width=2)
    center_text((cx - w // 2 + 8, cy - h // 2 + 8, cx + w // 2 - 8, cy + h // 2 - 8), text, FT_XS)
    return (cx - w // 2, cy - h // 2, cx + w // 2, cy + h // 2)


def polyline(points):
    for p1, p2 in zip(points, points[1:]):
        d.line((*p1, *p2), fill="black", width=2)


def label(x: int, y: int, text: str):
    d.text((x, y), text, font=FT_XS, fill="black")


# 中心实体：报修工单。
rect(800, 500, 160, 60, "报修工单")

# 左侧实体：用户、学生、公告、论坛、评价与类型。
rect(160, 500, 130, 55, "用户")
rect(390, 230, 150, 55, "学生档案")
rect(130, 230, 130, 55, "公告")
rect(130, 780, 150, 55, "论坛帖子")
rect(390, 780, 150, 55, "报修类型")
rect(640, 830, 150, 55, "维修评价")

# 上方实体：楼栋、宿舍、设施台账。
rect(600, 120, 130, 55, "楼栋")
rect(850, 120, 130, 55, "宿舍")
rect(1130, 120, 160, 55, "设施台账")

# 右侧实体：工单图片、流转、反馈、留言、耗材。
rect(1250, 330, 150, 55, "工单图片")
rect(1210, 500, 140, 55, "流转记录")
rect(1250, 670, 150, 55, "维修反馈")
rect(1460, 500, 140, 55, "服务留言")
rect(920, 900, 150, 55, "耗材库存")
rect(1250, 900, 150, 55, "耗材使用")

# 关系节点。
diamond(260, 365, 90, 48, "拥有")
diamond(260, 230, 90, 48, "发布")
diamond(260, 780, 90, 48, "发表")
diamond(560, 365, 90, 48, "提交")
diamond(560, 650, 90, 48, "分类")
diamond(700, 680, 90, 48, "评价")
diamond(725, 120, 90, 48, "包含")
diamond(980, 120, 90, 48, "配置")
diamond(820, 260, 105, 48, "选择地点")
diamond(1030, 260, 105, 48, "关联设施")
diamond(1040, 330, 90, 48, "包含")
diamond(1040, 500, 90, 48, "产生")
diamond(1040, 670, 90, 48, "完成")
diamond(1340, 500, 90, 48, "留言")
diamond(1250, 790, 90, 48, "上报")
diamond(1085, 900, 90, 48, "扣减")

# 左侧关系连线。
polyline([(225, 500), (260, 500), (260, 389)])
polyline([(260, 341), (390, 258)])
label(232, 455, "1")
label(316, 288, "1")

polyline([(195, 230), (215, 230), (260, 230), (315, 230)])
label(212, 210, "1")
label(302, 210, "n")

polyline([(205, 780), (260, 780), (315, 780)])
label(225, 760, "1")
label(302, 760, "n")

# 学生档案提交工单，折线绕开中心实体文字。
polyline([(465, 258), (500, 258), (500, 365), (515, 365)])
polyline([(605, 365), (720, 365), (720, 470)])
label(456, 238, "1")
label(690, 345, "n")

# 报修类型分类。
polyline([(465, 780), (500, 780), (500, 650), (515, 650)])
polyline([(605, 650), (740, 650), (740, 530)])
label(458, 760, "1")
label(704, 630, "n")

# 评价关系。
polyline([(640, 802), (640, 680), (655, 680)])
polyline([(745, 680), (790, 680), (790, 530)])
label(620, 740, "1")
label(768, 610, "1")

# 地点与设施关系。
polyline([(665, 120), (680, 120), (770, 120), (785, 120)])
label(708, 98, "1")
label(800, 98, "n")

polyline([(915, 120), (935, 120), (1025, 120), (1050, 120)])
label(920, 98, "1")
label(1010, 98, "n")

polyline([(850, 148), (850, 236)])
polyline([(820, 284), (820, 470)])
label(850, 190, "1")

polyline([(1130, 148), (1130, 236), (1058, 260)])
polyline([(1002, 260), (860, 260), (860, 470)])
label(1090, 190, "1")

# 工单右侧附属数据，分层展开。
polyline([(880, 500), (995, 500)])
polyline([(1085, 500), (1140, 500)])
label(900, 480, "1")
label(1160, 480, "n")

polyline([(880, 470), (980, 330), (995, 330)])
polyline([(1085, 330), (1175, 330)])
label(900, 360, "1")
label(1160, 310, "n")

polyline([(880, 530), (980, 670), (995, 670)])
polyline([(1085, 670), (1175, 670)])
label(900, 610, "1")
label(1160, 650, "1")

polyline([(1280, 500), (1295, 500)])
polyline([(1385, 500), (1390, 500)])
label(1350, 480, "n")

# 维修反馈、耗材使用、库存扣减。
polyline([(1250, 698), (1250, 766)])
polyline([(1250, 814), (1250, 872)])
label(1260, 730, "1")
label(1260, 840, "n")

polyline([(995, 900), (1040, 900)])
polyline([(1130, 900), (1175, 900)])
label(1005, 880, "1")
label(1150, 880, "n")

OUT.parent.mkdir(parents=True, exist_ok=True)
img.save(OUT)
print(OUT.resolve())
