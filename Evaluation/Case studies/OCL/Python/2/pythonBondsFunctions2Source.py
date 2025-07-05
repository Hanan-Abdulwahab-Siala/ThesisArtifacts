import time

def comp(date_str):
    return date_str.split('/')
  
def year(date_str):
    return int(comp(date_str)[-1])

def month(date_str):
    return int(comp(date_str)[-2])

def day(date_str):
    return int(comp(date_str)[-3])

def is_leap_year(year):
    
    if (year % 4 == 0 and year % 100 != 0) or (year % 400 == 0):
        return True
    return False


def calculate_days_between_dates(start_date_str, end_date_str):
    start_day = day(start_date_str)
    start_month = month(start_date_str)
    start_year = year(start_date_str)

    end_day = day(end_date_str)
    end_month = month(end_date_str)
    end_year = year(end_date_str)

    days = 0

    while start_year < end_year or (start_year == end_year and start_month < end_month):
        days_in_month = 30  
        if start_month == 2:
            # February
            if is_leap_year(start_year):
                days_in_month = 29
            else:
                days_in_month = 28
        elif start_month in [4, 6, 9, 11]:
            days_in_month = 30
        else:
            days_in_month = 31

        days += days_in_month - start_day + 1
        start_day = 1
        start_month += 1

        if start_month > 12:
            start_month = 1
            start_year += 1

    days += end_day - start_day

    return days

def days360(I,S,dc,M) : 
  return calculate_days_between_dates(I,S)

def straddle(I,S,p) : 
  return [I,S]

def acc(I,S,f,c,dc,M):
    p=int(12/f)
    st=straddle(I,S,p)
    if(dc=="Actual/365F"):
        aif=(calculate_days_between_dates(st[0],S)/365)*c
    elif(dc=="Actual/ActualISDA"):
        if(is_leap_year(year(st[0])) and is_leap_year(year(S))):
            aif=(calculate_days_between_dates(st[0],S)/366)*c
        elif(not(is_leap_year(year(st[0]))) and not(is_leap_year(year(S)))):
            aif=(calculate_days_between_dates(st[0],S)/365)*c
        elif(is_leap_year(year(st[0])) and not(is_leap_year(year(S)))):
            ys=str(year(st[0]))
            ye=str(year(S))
            aif=(calculate_days_between_dates(st[0],"31"+"/"+"12"+"/"+ys)/366)*c+\
            (calculate_days_between_dates("01"+"/"+"01"+"/"+ye,S)/365)*c
        else:
            ys=str(year(st[0]))
            ye=str(year(S))
            aif=(calculate_days_between_dates(st[0],"31"+"/"+"12"+"/"+ys)/365)*c+\
            (calculate_days_between_dates("01"+"/"+"01"+"/"+ye,S)/366)*c
    
    elif (dc=="Actual/364"):
        aif=(calculate_days_between_dates(st[0],S)/364)*c
    elif (dc=="Actual/360"):
        aif=(calculate_days_between_dates(st[0],S)/360)*c
    elif (dc=="Actual/ActualICMA"):
        aif=(calculate_days_between_dates(st[0],S)/(f*calculate_days_between_dates(st[0],st[1])))*c
    else:
        aif=(days360(st[0],S,dc,M)/360)*c
    return aif
    


print(calculate_days_between_dates("31/07/2020", "31/08/2020"))
print(acc("31/07/2020", "31/08/2020", 2, 0.02, "Actual/360", "31/07/2024"))

t1 = time.time()

for yr in range(1800,4024) : 
  for mnt in range(10,13) : 
    for dd in range(10,31) :
       acc("01/" + str(mnt) + "/" + str(yr-1), 
           str(dd) + "/" + str(mnt) + "/" + str(yr), 
           2 , 0.02, "Actual/ActualISDA", "31/07/2024")

t2 = time.time()
print(1000*(t2 - t1))
