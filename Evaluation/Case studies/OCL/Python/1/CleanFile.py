def comp(date_str):
    return date_str.split('/')
def year(date_str):
    return int(comp(date_str)[-1])
def month(date_str):
    return int(comp(date_str)[-2])
def day(date_str):
    return int(comp(date_str)[-3])
def MaxDate(Date1, Date2):
    day1, month1, year1 = day(Date1), month(Date1), year(Date1)
    day2, month2, year2 = day(Date2), month(Date2), year(Date2)
    if year1 > year2:
        return Date1
    elif year1 < year2:
        return Date2
    else:
        if month1 > month2:
            return Date1
        elif month1 < month2:
            return Date2
        else:
            if day1 >= day2:
                return Date1
            else:
                return Date2
def Prevd(D,p):
    if(month(D)-p)<=0:
         dd=str(day(D))
         mm=str((12 - (p  - month(D))))
         yy=str(year(D) - 1)
         if(day(D)<10):
             dd='0'+dd
         if (12 - (p  - month(D))<10):
             mm='0'+mm
    else:
         dd=str(day(D))
         mm=str((month(D) - p ))
         yy=str(year(D))
         if(day(D)<10):
             dd='0'+dd
         if ((month(D) - p)<10):
             mm='0'+mm
    return dd + '/' + mm + '/' + yy
def Nextd(D, p):
    if (month(D) + p) > 12:
        dd=str(day(D))
        mm=str((month(D) + p) % 12)
        yy=str(year(D) + 1)
        if(day(D)<10):
            dd='0'+dd
        if(((month(D) + p) % 12)<10):
            mm='0'+mm
    else:
        dd=str(day(D))
        mm=str(month(D) + p)
        yy=str(year(D))
    return dd + '/' + mm + '/' + yy