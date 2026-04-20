<?php

declare(strict_types=1);

namespace App\Repository;

use App\Config\Config;
use PDO;

/**
 * Erzeugt PDO-Verbindungen für Lesepfad und Schreibpfad.
 *
 * Die Anwendung unterstützt getrennte Accounts. Falls das Hosting nur einen
 * DB-Benutzer zulässt, können dieselben Zugangsdaten in `.env` gesetzt werden.
 */
final class DbConnectionFactory
{
    public function __construct(private readonly Config $config)
    {
    }

    public function createReadConnection(): PDO
    {
        return $this->createConnection(
            $this->config->getString('DB_READ_USER'),
            $this->config->getString('DB_READ_PASS')
        );
    }

    public function createWriteConnection(): PDO
    {
        return $this->createConnection(
            $this->config->getString('DB_WRITE_USER'),
            $this->config->getString('DB_WRITE_PASS')
        );
    }

    private function createConnection(string $user, string $password): PDO
    {
        $dsn = sprintf(
            'mysql:host=%s;port=%s;dbname=%s;charset=%s',
            $this->config->getString('DB_HOST'),
            $this->config->getString('DB_PORT', '3306'),
            $this->config->getString('DB_NAME'),
            $this->config->getString('DB_CHARSET', 'utf8mb4')
        );

        return new PDO(
            $dsn,
            $user,
            $password,
            [
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES => false,
            ]
        );
    }
}
