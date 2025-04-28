$(document).ready(function() {
    const token = $('#token').val();
    const backendApiUrl = $('#backendApiUrl').val();
    console.log('Token:', token);
    console.log('Backend API URL:', backendApiUrl);

    // Initialize DataTable for active customers
    const activeTable = $('#activeCustomersTable').DataTable({
        ajax: {
            url: backendApiUrl + '/customers/active',
            dataSrc: function(json) {
                // Map the response to match the expected structure
                const mappedData = json.map(item => ({
                    id: item.id,
                    username: item.username,
                    email: item.email,
                    phoneNumber: item.phoneNumber
                }));
                console.log('Mapped active customers data:', mappedData);
                return mappedData;
            },
            headers: {
                'Authorization': 'Bearer ' + token
            },
            error: function(xhr, error, thrown) {
                console.error('Error fetching active customers:', xhr.status, xhr.responseText);
                alert('Failed to load active customers. Status: ' + xhr.status + ', Error: ' + xhr.responseText);
            }
        },
        columns: [
            { data: 'id' },
            { data: 'username' },
            { data: 'email' },
            { data: 'phoneNumber' },
            {
                data: null,
                render: function(data, type, row) {
                    console.log('Rendering active customer row:', row);
                    return `
                        <a href="/customers/edit/${row.id}" class="btn btn-sm btn-warning">Edit</a>
                        <button class="btn btn-sm btn-danger" onclick="deactivateCustomer(${row.id})">Deactivate</button>
                    `;
                },
                orderable: false,
                searchable: false
            }
        ],
        dom: 'Bfrtip',
        buttons: ['copy', 'csv', 'excel'],
        pageLength: 5,
        autoWidth: false,
        ordering: true,
        searching: true,
        paging: true,
        language: {
            search: "Search customers:",
            emptyTable: "No active customers found",
            info: "Showing _START_ to _END_ of _TOTAL_ customers",
            infoEmpty: "Showing 0 to 0 of 0 customers",
            lengthMenu: "Show _MENU_ customers per page"
        },
        initComplete: function(settings, json) {
            console.log('Active customers table initialized with data:', json);
        },
        drawCallback: function(settings) {
            console.log('Active customers table drawn with data length:', settings.aoData.length);
        }
    });

    // Initialize DataTable for inactive customers
    const inactiveTable = $('#inactiveCustomersTable').DataTable({
        ajax: {
            url: backendApiUrl + '/customers/inactive',
            dataSrc: function(json) {
                const mappedData = json.map(item => ({
                    id: item.id,
                    username: item.username,
                    email: item.email,
                    phoneNumber: item.phoneNumber
                }));
                console.log('Mapped inactive customers data:', mappedData);
                return mappedData;
            },
            headers: {
                'Authorization': 'Bearer ' + token
            },
            error: function(xhr, error, thrown) {
                console.error('Error fetching inactive customers:', xhr.status, xhr.responseText);
                alert('Failed to load inactive customers. Status: ' + xhr.status + ', Error: ' + xhr.responseText);
            }
        },
        columns: [
            { data: 'id' },
            { data: 'username' },
            { data: 'email' },
            { data: 'phoneNumber' },
            {
                data: null,
                render: function(data, type, row) {
                    console.log('Rendering inactive customer row:', row);
                    return `
                        <a href="/customers/edit/${row.id}" class="btn btn-sm btn-warning">Edit</a>
                        <button class="btn btn-sm btn-success" onclick="reactivateCustomer(${row.id})">Reactivate</button>
                    `;
                },
                orderable: false,
                searchable: false
            }
        ],
        dom: 'Bfrtip',
        buttons: ['copy', 'csv', 'excel'],
        pageLength: 5,
        autoWidth: false,
        ordering: true,
        searching: true,
        paging: true,
        language: {
            search: "Search customers:",
            emptyTable: "No inactive customers found",
            info: "Showing _START_ to _END_ of _TOTAL_ customers",
            infoEmpty: "Showing 0 to 0 of 0 customers",
            lengthMenu: "Show _MENU_ customers per page"
        },
        initComplete: function(settings, json) {
            console.log('Inactive customers table initialized with data:', json);
        },
        drawCallback: function(settings) {
            console.log('Inactive customers table drawn with data length:', settings.aoData.length);
        }
    });

    // Ensure tables are visible
    $('#activeCustomersTable').show();
    $('#inactiveCustomersTable').show();
});

// Functions to handle deactivate/reactivate actions
function deactivateCustomer(id) {
    const token = $('#token').val();
    const backendApiUrl = $('#backendApiUrl').val();
    fetch(backendApiUrl + '/customers/' + id + '/deactivate', {
        method: 'PUT',
        headers: {
            'Authorization': 'Bearer ' + token
        }
    }).then(response => {
        if (response.ok) {
            alert('Customer deactivated successfully');
            $('#activeCustomersTable').DataTable().ajax.reload();
            $('#inactiveCustomersTable').DataTable().ajax.reload();
        } else {
            alert('Failed to deactivate customer');
        }
    }).catch(error => {
        console.error('Error deactivating customer:', error);
        alert('Error deactivating customer');
    });
}

function reactivateCustomer(id) {
    const token = $('#token').val();
    const backendApiUrl = $('#backendApiUrl').val();
    fetch(backendApiUrl + '/customers/' + id + '/reactivate', {
        method: 'PUT',
        headers: {
            'Authorization': 'Bearer ' + token
        }
    }).then(response => {
        if (response.ok) {
            alert('Customer reactivated successfully');
            $('#activeCustomersTable').DataTable().ajax.reload();
            $('#inactiveCustomersTable').DataTable().ajax.reload();
        } else {
            alert('Failed to reactivate customer');
        }
    }).catch(error => {
        console.error('Error reactivating customer:', error);
        alert('Error reactivating customer');
    });
}