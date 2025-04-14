import serial
import time
import random
import pytest

# Параметры порта и настройки
PORT = "COM3"
BAUDRATE = 9600
TIMEOUT = 2

# Шаблоны ответов
RESPONSES = {
    "t": [
        "END temperature = 29.042011 ;",
        "END temperature = 37.363857 ;",
        "END temperature = 37.403576 ;",
    ],
    "v": [
        "END freq1 = 8790686.000000 freq2 = 14990853.000000  u0min = 0.397160 u0max = 0.545117 din_naklon = 0.000000 fmiss = -90000.000000"
    ],
    "V": [
        "END freq1 = 8780652.000000 freq2 = 14980820.000000  u0min = 0.400971 u0max = 0.549708 din_naklon = 0.000000 fmiss = -90000.000000"
    ],
    ">": [
        "END Ulaser = 1.700000 U_start = 2.700000 U_stop = 3.000000  dU_laser = -0.300000 ;",
        "END Ulaser = 1.800000 U_start = 2.800000 U_stop = 3.100000  dU_laser = -0.200000 ;",
        "END Ulaser = 1.900000 U_start = 2.900000 U_stop = 3.200000  dU_laser = -0.100000 ;",
    ],
    "<": [
        "END Ulaser = 2.000000 U_start = 3.000000 U_stop = 3.300000  dU_laser = 0.000000 ;",
        "END Ulaser = 2.100000 U_start = 3.100000 U_stop = 3.400000  dU_laser = 0.100000 ;",
        "END Ulaser = 2.200000 U_start = 3.200000 U_stop = 3.500000  dU_laser = 0.200000 ;",
    ],
    "h": ["END LASER ON ;", "END LASER OFF ;"],
}

def handle_request(data):
    """
    Возвращает ответ на полученную команду.
    """
    key = data.strip().lower() if data.lower() != "v" else data
    if key in RESPONSES:
        response = random.choice(RESPONSES[key])
        return response
    else:
        return "END UNKNOWN COMMAND ;"

def listen_and_respond(ser):
    """
    Слушает команды и отвечает на них.
    """
    print("Ожидание команд от устройства...")
    while True:
        try:
            if ser.in_waiting > 0:
                data = ser.read(ser.in_waiting).decode("utf-8").strip()
                print(f"Принято: {data}")

                # Убираем символ 'r' в конце, если он есть
                if data.endswith("r"):
                    data = data[:-1]

                response = handle_request(data)
                time.sleep(0.5)  # небольшая задержка, имитация обработки
                ser.write(response.encode("utf-8"))
                print(f"Ответ отправлен: {response}")
        except serial.SerialException as e:
            print(f"Ошибка соединения: {e}")
            break
        except KeyboardInterrupt:
            print("Работа завершена пользователем.")
            break

def send_command(command):
    """Отправка команды и получение ответа от устройства"""
    with serial.Serial(PORT, baudrate=BAUDRATE, timeout=TIMEOUT) as ser:
        ser.write((command + "r").encode("utf-8"))  # добавляем символ 'r' как завершение команды
        time.sleep(0.5)
        response = ser.read(ser.in_waiting or 100).decode("utf-8").strip()
        return response


@pytest.mark.parametrize("command", ["t", ">", "<", "h"])
def test_responses_change(command):
    """Тест для команд с динамическим ответом"""
    print(f"Теперь отправь команду {command} с устройства...")

    response = ""
    while not response:  # Ожидаем получения ответа
        response = send_command(command)

    print(f"Ответ: {response}")
    assert response != "", "Ответ не может быть пустым"
    assert response.startswith("END"), "Ответ должен начинаться с 'END'"
    assert "temperature" in response or "Ulaser" in response, "Ответ не содержит ожидаемых данных"


@pytest.mark.parametrize("command", ["v", "V"])
def test_responses_constant(command):
    """Тест для команд с одинаковым ответом"""
    print(f"Теперь отправь команду {command} с устройства...")

    response = ""
    while not response:  # Ожидаем получения ответа
        response = send_command(command)

    print(f"Ответ: {response}")
    assert response != "", "Ответ не может быть пустым"
    assert response.startswith("END"), "Ответ должен начинаться с 'END'"
    assert "freq" in response, "Ответ не содержит частоты"


@pytest.mark.parametrize("command", ["x", "z", "1", "#"])
def test_unknown_commands(command):
    """Тест для неизвестных команд"""
    print(f"Теперь отправь команду {command} с устройства...")

    response = ""
    while not response:  # Ожидаем получения ответа
        response = send_command(command)

    print(f"Ответ: {response}")
    assert response.startswith("END"), "Ответ должен начинаться с 'END'"
    assert "UNKNOWN" in response, "Ответ не содержит сообщения об ошибке"

# Основная функция для запуска эмулятора
def start_emulator(port):
    try:
        ser = serial.Serial(port, baudrate=9600, timeout=1)
        print(f"Эмуляция устройства запущена на порту {port}")
        listen_and_respond(ser)
    except serial.SerialException as e:
        print(f"Ошибка подключения к порту {port}: {e}")


if __name__ == "__main__":
    print("Запуск эмулятора и тестов...")
    # Запускаем эмулятор в отдельном процессе или потоке
    from threading import Thread
    emulator_thread = Thread(target=start_emulator, args=(PORT,))
    emulator_thread.daemon = True
    emulator_thread.start()

    # Запускаем тесты
    time.sleep(20)  # Даем эмулятору немного времени на старт
    pytest.main()
