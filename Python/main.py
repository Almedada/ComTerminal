import serial
import time


def connect_bluetooth(port, baudrate=9600):
    """
    Устанавливает соединение с Bluetooth-портом через COM.
    """
    try:
        # Открытие Bluetooth-порта
        ser = serial.Serial(port, baudrate, timeout=1)
        print(f"Соединение с {port} установлено.")
        return ser
    except serial.SerialException as e:
        print(f"Ошибка подключения к порту {port}: {e}")
        return None


def listen_for_data(ser):
    """
    Слушает данные, поступающие через Bluetooth-порт.
    """
    while True:
        try:
            # Чтение доступных данных
            if ser.in_waiting > 0:
                data = ser.read(ser.in_waiting)  # Чтение всех доступных данных
                print(f"Получены данные: {data.decode('utf-8')}")

            time.sleep(1)  # Пауза между циклами
        except serial.SerialException as e:
            print(f"Ошибка при получении данных: {e}")
            break
        except KeyboardInterrupt:
            print("Ожидание прервано пользователем.")
            break


def main():
    # Настройка COM-порта для подключения Bluetooth
    port = "COM5"  # Замените на свой COM-порт, например COM4, COM5 и т.д.
    baudrate = 9600  # Скорость передачи данных

    # Подключаемся к Bluetooth-серверу
    ser = connect_bluetooth(port, baudrate)

    if ser:
        # Если соединение установлено, начинаем слушать данные
        listen_for_data(ser)

        # Закрываем порт после завершения работы
        ser.close()
        print("Порт закрыт.")


if __name__ == "__main__":
    main()
